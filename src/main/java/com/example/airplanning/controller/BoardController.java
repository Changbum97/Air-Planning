package com.example.airplanning.controller;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentDtoWithCoCo;
import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final CommentService commentService;
    private final PlannerService plannerService;
    private final LikeService likeService;
    private final RegionService regionService;

    @GetMapping("/list")
    public String listBoard(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                            Model model,
                            @RequestParam(required = false) String searchType,
                            @RequestParam(required = false) String keyword){


        Page<BoardListResponse> boardPage = boardService.boardList(pageable, searchType, keyword);
        model.addAttribute("list", boardPage);
        model.addAttribute("boardSearchRequest", new BoardSearchRequest(searchType, keyword));

        return "boards/list";
    }

    @GetMapping("/write")
    public String writeBoardPage(Model model) {
        model.addAttribute(new BoardCreateRequest());
        return "boards/write";
    }

    @ResponseBody
    @PostMapping("/write")
    public String writeBoard(@RequestPart(value = "request") BoardCreateRequest req,
                             @RequestPart(value = "file",required = false) MultipartFile file, Principal principal) throws IOException {

        try {
            boardService.writeWithFile(req, file, principal.getName(), Category.FREE);
        } catch (AppException e) {
            if  (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return "파일 업로드 과정 중 오류가 발생했습니다. 다시 시도 해주세요.*/boards/write";
            }
        } catch (Exception e) {
            return "error*/";
        }

        return "글이 등록되었습니다.*/boards/list";

    }

    @GetMapping("/{boardId}")
    public String detailBoard(@PathVariable Long boardId, Model model, Principal principal,
                              @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                              HttpServletRequest request, HttpServletResponse response){

        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();
        Boolean addView = true;
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("boardView")) {
                    oldCookie = cookie;
                    break;
                }
            }
        }
        if(oldCookie != null && oldCookie.getValue().equals(boardId.toString())) {
                addView = false;
        } else {
            Cookie newCookie = new Cookie("boardView", boardId.toString());
            newCookie.setMaxAge(60 * 60);   // 한 시간
            response.addCookie(newCookie);
        }

        BoardDto boardDto = boardService.detail(boardId, addView);
        model.addAttribute("board", boardDto);

        Page<CommentDtoWithCoCo> commentPage = commentService.readBoardParentCommentOnly(boardId, pageable);
        Page<CommentDto> commentSize = commentService.readPage(boardId, "BOARD_COMMENT", pageable);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("commentCreateRequest", new CommentCreateRequest());
        model.addAttribute("commentSize", commentSize.getTotalElements());

        if(principal != null) {
            model.addAttribute("checkLike", likeService.checkLike(boardId, principal.getName()));

            // 로그인 유저가 글 작성자라면 수정, 삭제 버튼 출력
            if(principal.getName().equals(boardDto.getUserName())) {
                model.addAttribute("isWriter", true);
            }
        } else {
            model.addAttribute("checkLike", false);
        }
        return "boards/detail";
    }

    @ResponseBody
    @PostMapping("/{boardId}/modify")
    public String modifyBoard(@PathVariable Long boardId, @RequestPart(value = "request") BoardModifyRequest req,
                              @RequestPart(value = "file",required = false) MultipartFile file,  Principal principal) {
        try {
            boardService.modify(req, file, principal.getName(), boardId);
        } catch (AppException e) {
            if (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return "파일 업로드 과정 중 오류가 발생했습니다. 다시 시도 해주세요.*/boards/" + boardId;
            } else if (e.getErrorCode().equals(ErrorCode.BOARD_NOT_FOUND)) {
                return "게시글이 존재하지 않습니다.*/";
            } else if (e.getErrorCode().equals(ErrorCode.INVALID_PERMISSION)) { //작성자 수정자 불일치 (혹시 버튼이 아닌 url로 접근시 제한)
                return "작성자만 수정이 가능합니다.*/boards/" + boardId;
            }
        } catch (Exception e){ //알수 없는 error
            return "error*/";
        }

        return "글 수정을 완료했습니다.*/boards/" + boardId;
    }

    @GetMapping("/{boardId}/modify")
    public String modifyBoardPage(@PathVariable Long boardId, Model model, Principal principal){
        Board board = boardService.view(boardId);
        if (!board.getUser().getUserName().equals(principal.getName())) {
            model.addAttribute("msg", "작성자만 수정가능합니다.");
            model.addAttribute("nextPage", "/boards/" + boardId);
            return "error/redirect";
        }
        model.addAttribute(new BoardModifyRequest(board.getTitle(), board.getContent(), board.getImage()));
        return "boards/modify";
    }

    // 플래너등급신청
    @GetMapping("/rankUpWrite")
    public String rankUpWrite(Model model) {
        model.addAttribute(new RankUpCreateRequest());

        List<Region> regions = regionService.findAll();
        HashSet<String> region1List = new HashSet<>();
        for (Region region : regions) {
            region1List.add(region.getRegion1());
        }

        model.addAttribute("region1List", region1List);
        model.addAttribute("regions", regions);
        return "boards/rankUpWrite";
    }

    @ResponseBody
    @PostMapping("/rankUpWrite")
    public String rankUpWrite(RankUpCreateRequest createRequest, Principal principal){
        boardService.rankUpWrite(createRequest, principal.getName());
        return "등급업 신청 성공";
    }

    // 플래너신청조회
    @GetMapping("/rankUp/{boardId}")
    public String rankUpDetail(@PathVariable Long boardId, Principal principal, Model model, @AuthenticationPrincipal UserDetail userDetail){
        RankUpDetailResponse rankUpDetailResponse = boardService.rankUpDetail(boardId);
        model.addAttribute("board", rankUpDetailResponse);
        model.addAttribute("userName", principal.getName());
        model.addAttribute("role", userDetail.getRole());
        return "boards/rankUpDetail";
    }

    @GetMapping("/rankUp/update/{boardId}")
    public String rankUpdate(@PathVariable Long boardId, Model model){
        Board board = boardService.update(boardId);
//        model.addAttribute(new BoardModifyRequest(board.getTitle(), board.getContent()));
        return "boards/rankUpdate";
    }

    @PostMapping("/rankUp/update/{boardId}")
    public String rankUpdate(@PathVariable Long boardId, BoardModifyRequest boardModifyRequest, Principal principal, Model model){
        boardService.rankUpdate(boardModifyRequest, principal.getName(), boardId);
        model.addAttribute("boardId", boardId);
        return "redirect:/boards/rankUp/{boardId}";
    }

    @ResponseBody
    @GetMapping("/rankUp/delete/{boardId}")
    public String rankDelete(@PathVariable Long boardId, Principal principal){
        boardService.rankDelete(boardId, principal.getName());
        log.info("delete");

        return "";
    }

    // 포트폴리오 리스트
    @GetMapping("/portfolio/list")
    public String portfolioList(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                            Model model,
                            @RequestParam(required = false) String searchType,
                            @RequestParam(required = false) String keyword){

        Page<BoardListResponse> boardPage = boardService.portfolioList(pageable, searchType, keyword);
        model.addAttribute("list", boardPage);
        model.addAttribute("boardSearchRequest", new BoardSearchRequest(searchType, keyword));

        return "boards/portfolioList";
    }

    // 포트폴리오 작성
    @GetMapping("/portfolio/write")
    public String portfolioWrite(Model model, Principal principal) {

        PlannerDetailResponse response = plannerService.findByUser(principal.getName());
        
        model.addAttribute(new BoardCreateRequest());
        model.addAttribute("planner", response);
        return "boards/portfolioWrite";
    }

    @ResponseBody
    @PostMapping("/portfolio/write")
    public String portfolioWrite(@RequestPart(value = "request") BoardCreateRequest req,
                                 @RequestPart(value = "file",required = false) MultipartFile file, Principal principal) throws IOException {

        Long boardId = Long.valueOf(0);

        try {
            boardId = boardService.writeWithFile(req, file, principal.getName(),Category.PORTFOLIO);
        } catch (AppException e) {
            if  (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return "파일 업로드 과정 중 오류가 발생했습니다. 다시 시도 해주세요.*/boards/portfolio/write";
            }
        } catch (Exception e) {
            return "error*/";
        }

        return "글이 등록되었습니다.*/boards/portfolio/"+boardId;
    }

    //포토폴리오 상세
    @GetMapping("/portfolio/{boardId}")
    public String portfolioDetail(@PathVariable Long boardId, Model model, Principal principal,
                                  @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                  HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        Cookie oldCookie = null;
        Cookie[] cookies = httpServletRequest.getCookies();
        Boolean addView = true;
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("portfolioView")) {
                    oldCookie = cookie;
                    break;
                }
            }
        }
        if(oldCookie != null && oldCookie.getValue().equals(boardId.toString())) {
            addView = false;
        } else {
            Cookie newCookie = new Cookie("portfolioView", boardId.toString());
            newCookie.setMaxAge(60 * 60);   // 한 시간
            httpServletResponse.addCookie(newCookie);
        }

        BoardDto boardDto = boardService.portfolioDetail(boardId, addView);
        PlannerDetailResponse response = plannerService.findByUser(boardDto.getUserName());

        model.addAttribute("planner", response);
        model.addAttribute("board", boardDto);

        if (principal != null) {
            model.addAttribute("checkLike", likeService.checkLike(boardId, principal.getName()));
            // 로그인 유저가 글 작성자라면 수정, 삭제 버튼 출력
            if(principal.getName().equals(boardDto.getUserName())) {
                model.addAttribute("isWriter", true);
            }
        } else {
            model.addAttribute("checkLike", false);
        }

        Page<CommentDtoWithCoCo> commentPage = commentService.readBoardParentCommentOnly(boardId, pageable);
        Page<CommentDto> commentSize = commentService.readPage(boardId, "BOARD_COMMENT", pageable);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("commentCreateRequest", new CommentCreateRequest());
        model.addAttribute("commentSize", commentSize.getTotalElements());

        return "boards/portfolioDetail";
    }

    //포토폴리오 게시글 수정
    @GetMapping("portfolio/{boardId}/modify")
    public String portfolioModify(@PathVariable Long boardId, Model model, Principal principal){

        PlannerDetailResponse response = plannerService.findByUser(principal.getName());
        BoardDto boardDto = boardService.portfolioDetail(boardId, false);
        model.addAttribute("planner", response);
        model.addAttribute(new BoardModifyRequest(boardDto.getTitle(), boardDto.getContent(), boardDto.getImage()));
        return "boards/portfolioModify";
    }

    @ResponseBody
    @PostMapping("portfolio/{boardId}/modify")
    public String portfolioModify(@PathVariable Long boardId, @RequestPart(value = "request") BoardModifyRequest req,
                                  @RequestPart(value = "file",required = false) MultipartFile file,  Principal principal, Model model) throws IOException {

        log.info(req.getImage());

        try {
            boardService.modify(req, file, principal.getName(), boardId);
        } catch (AppException e) {
            if (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return "파일 업로드 과정 중 오류가 발생했습니다. 다시 시도 해주세요.*/boards/portfolio/" + boardId;
            } else if (e.getErrorCode().equals(ErrorCode.BOARD_NOT_FOUND)) {
                return "게시글이 존재하지 않습니다.*/";
            } else if (e.getErrorCode().equals(ErrorCode.INVALID_PERMISSION)) { //작성자 수정자 불일치 (혹시 버튼이 아닌 url로 접근시 제한)
                return "작성자만 수정이 가능합니다.*/boards/portfolio/" + boardId;
            }
        } catch (Exception e){ //알수 없는 error
            return "error*/";
        }

        return "글 수정을 완료했습니다.*/boards/portfolio/" + boardId;
    }

    //포토폴리오 게시글 삭제
    @ResponseBody
    @GetMapping("portfolio/{boardId}/delete")
    public String portfolioDelete(@PathVariable Long boardId, Principal principal){

        boardService.delete(principal.getName(), boardId);
        log.info("delete");

        return "";
    }

    @PostMapping("/{boardId}/like")
    @ResponseBody
    public String changeLike(@PathVariable Long boardId, Principal principal) {
        return likeService.changeLike(boardId, principal.getName());
    }

    // 유저 신고 작성
    @GetMapping("/report/write")
    public String reportWritePage(Model model) {
        model.addAttribute(new ReportCreateRequest());
        return "boards/reportWrite";
    }

    @PostMapping("/report/write")
    public String reportWrite(ReportCreateRequest reportCreateRequest, Principal principal){
        boardService.reportWrite(reportCreateRequest, principal.getName());
        return "redirect:/boards/list";
    }


}
