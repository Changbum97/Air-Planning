package com.example.airplanning.controller.api;


import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.board.BoardDto;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.BoardService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@Slf4j
public class BoardRestController {

    private final BoardService boardService;

    // 게시판 리스트
    @GetMapping("/{category}/list")
    @ApiOperation(value = "게시판 리스트 조회", notes = "게시판 리스트를 조회합니다. 누구나 조회 가능하며, 제목과 작성자로 검색 할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "category", value = "게시판 카테고리, FREE, RANK_UP, REPORT, PORTFOLIO 중 하나를 선택", defaultValue = "free"),
            @ApiImplicitParam(name = "searchType", value = "검색 조건, TITLE, NICKNAME 중 하나를 입력", defaultValue = "TITLE"),
            @ApiImplicitParam(name = "keyword", value = "검색어", defaultValue = "None")})
    public Response<?> listBoard(@ApiIgnore @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                 @PathVariable Category category,
                                 @RequestParam(required = false) String searchType,
                                 @RequestParam(required = false) String keyword) {

        if (searchType != null) searchType = searchType.toUpperCase();

        Page<BoardListResponse> boardPage;

        if (category.equals(Category.PORTFOLIO)) {
            boardPage = boardService.portfolioList(pageable, searchType, keyword, null, 998L);
            return Response.success(boardPage);
        } else {
            boardPage = boardService.boardList(pageable, searchType, keyword, category);
            return Response.success(boardPage);
        }
    }

    // 게시판 글 작성
    @PostMapping("/{category}")
    public Response<?> writeBoard(@PathVariable String category, Principal principal,
                                  @RequestPart(value = "request") BoardCreateRequest req,
                                  @RequestPart(value = "file",required = false) MultipartFile file) throws IOException {

        category = category.toLowerCase();
        Category enumCategory;

        if (category.equals("free")) enumCategory = Category.FREE;
        else if (category.equals("report")) enumCategory = Category.REPORT;
        else if (category.equals("rankup")) enumCategory = Category.RANK_UP;
        else if (category.equals("portfolio")) enumCategory = Category.PORTFOLIO;
        else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        try {
            return Response.success(boardService.writeWithFile(req, file, principal.getName(), enumCategory));
        } catch (AppException e) {
            if  (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return Response.error("파일 업로드 과정 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        }
        return Response.error("에러 발생");
    }

    @GetMapping("/{category}/{boardId}")
    @ApiOperation(value = "게시판 글 조회", notes = "게시판 글 하나를 조회합니다. 자유게시판과 포트폴리오 게시판의 글에는 누구나 조회 가능하지만, 신고 게시판과 등업 게시판은 작성자와 ADMIN만 조회 가능합니다.")
    @ApiImplicitParams({@ApiImplicitParam(name = "category", value = "게시판 카테고리, FREE, RANK_UP, REPORT, PORTFOLIO 중 하나를 선택", defaultValue = "FREE")})
    public Response<?> detailBoard(@PathVariable Long boardId, @PathVariable Category category,
                                   @ApiIgnore Principal principal, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail){

        BoardDto boardDto = boardService.detail(boardId, false, category);

        if (category.equals(Category.REPORT) || category.equals(Category.RANK_UP)) {
            // 작성자 본인이거나 ADMIN이 아니면 에러 발생
            if (principal.getName().equals(boardDto.getUserName()) || userDetail.getRole().equals("ADMIN")) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        return Response.success(boardDto);
    }

    @PutMapping("/{category}/{boardId}")
    public Response<?> updateBoard(@PathVariable String category, @PathVariable Long boardId,
                                   @RequestPart(value = "request") BoardUpdateRequest req,
                                   @RequestPart(value = "file",required = false) MultipartFile file,
                                   Principal principal) {
        category = category.toLowerCase();
        Category enumCategory;

        if (category.equals("free")) enumCategory = Category.FREE;
        else if (category.equals("report")) enumCategory = Category.REPORT;
        else if (category.equals("rankup")) enumCategory = Category.RANK_UP;
        else if (category.equals("portfolio")) enumCategory = Category.PORTFOLIO;
        else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        try {
            boardService.modify(req, file, principal.getName(), boardId);
        } catch (AppException e) {
            if (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return Response.error("파일 업로드 과정 중 오류가 발생했습니다. 다시 시도해 주세요.");
            } else if (e.getErrorCode().equals(ErrorCode.BOARD_NOT_FOUND)) {
                return Response.error("게시글이 존재하지 않습니다.");
            } else if (e.getErrorCode().equals(ErrorCode.INVALID_PERMISSION)) { //작성자 수정자 불일치 (혹시 버튼이 아닌 url로 접근시 제한)
                return Response.error("작성자만 수정이 가능합니다.");
            }
        } catch (Exception e){ //알수 없는 error
            return Response.error("error");
        }

        return Response.success("글 수정을 완료했습니다.");
    }







    // 삭제
    @DeleteMapping("/{boardId}")
    public Response<String> delete(@PathVariable Long boardId, Principal principal) {

        boardService.delete(principal.getName(), boardId);
        return Response.success("글 삭제가 완료되었습니다.");
    }


    // 플래너 신청 등록
    @PostMapping("/rankUpWrite/{boardId}")
    public Response<BoardResponse>rankUpWrite(@RequestBody BoardCreateRequest boardCreateRequest, Principal principal) {
        String userName = principal.getName();
        BoardDto boardDto = boardService.write(boardCreateRequest, "test");
        return Response.success(new BoardResponse("프래너 등급 신청이 완료되었습니다.", boardDto.getId()));
    }

    // 플래너 신청 조회
    /*@GetMapping("/rankUp/{boardId}")
    @Operation(summary = "플래너 신청 조회")
    public Response<RankUpDetailResponse> rankUpDetail(@PathVariable Long boardId){
        return Response.success( boardService.rankUpDetail(boardId));
    }*/

    // 유저 신고 작성
    @PostMapping("/reportWrite/{boardId}")
    public Response<BoardResponse>reportWrite(@RequestBody ReportCreateRequest reportCreateRequest, Principal principal) throws IOException {
        String userName = principal.getName();
        Board board = boardService.reportWrite(reportCreateRequest, null, userName);
        return Response.success(new BoardResponse("신고가 완료되었습니다.", board.getId()));
    }
    

    // 유저 신고 상세 조회
    /*@GetMapping("/reportDetail/{boardId}")
    public Response<BoardDto> reportDetail(@PathVariable Long boardId) {
        BoardDto boardDto =  boardService.reportDetail(boardId);
        return Response.success(boardDto);
    }*/



    // 유저 신고 삭제
    @DeleteMapping("/reportDelete/{boardId}")
    public Response<BoardResponse> reportDelete(@PathVariable Long boardId, Principal principal) {
        String userName = principal.getName();
        Long deleteReport = boardService.reportDelete("test", boardId);
        return Response.success(new BoardResponse("신고 삭제가 완료되었습니다.", boardId));
    }

}

