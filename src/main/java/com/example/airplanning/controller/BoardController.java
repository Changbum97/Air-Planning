package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardModifyRequest;
import com.example.airplanning.domain.dto.board.PortfolioModifyRequest;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentDtoWithCoCo;
import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.BoardService;
import com.example.airplanning.service.CommentService;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.PlannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;


@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final PlannerService plannerService;
    private final LikeService likeService;

    @ResponseBody
    @PostMapping("")
    public String writeBoard(BoardCreateRequest createRequest, Principal principal){
        boardService.write(createRequest, principal.getName());
        return "redirect:/boards/{boardId}";
    }

    @GetMapping("/new/write")
    public String writeBoard(Model model) {
        model.addAttribute(new BoardCreateRequest());
        return "boards/write";
    }

    @GetMapping("/{boardId}")
    public String detailBoard(@PathVariable Long boardId, Model model, Principal principal,
                              @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        BoardDto boardDto = boardService.detail(boardId);
        model.addAttribute("board", boardDto);

        Page<CommentDtoWithCoCo> commentPage = commentService.readBoardParentCommentOnly(boardId, pageable);
        Page<CommentDto> commentSize = commentService.readPage(boardId, "BOARD_COMMENT", pageable);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("commentCreateRequest", new CommentCreateRequest());
        model.addAttribute("commentSize", commentSize.getTotalElements());

        if(principal != null) {
            model.addAttribute("checkLike", likeService.checkLike(boardId, principal.getName()));
        } else {
            model.addAttribute("checkLike", false);
        }
        return "boards/detail";
    }


    @GetMapping("/{boardId}/modify")
    public String modifyBoardPage(@PathVariable Long boardId, Model model){
        Board board = boardService.view(boardId);
        model.addAttribute(new BoardModifyRequest(board.getTitle(), board.getContent()));
        return "boards/modify";
    }

    @PostMapping("/{boardId}/modify")
    public String modifyBoard(@PathVariable Long boardId, BoardModifyRequest boardModifyRequest, Principal principal, Model model){
        boardService.modify(boardModifyRequest, principal.getName(), boardId);
        model.addAttribute("boardId", boardId);
        return "redirect:/boards/{boardId}";
    }

    // 플래너등급신청
    @GetMapping("/rankUpWrite")
    public String rankUpWrite(Model model) {
        model.addAttribute(new BoardCreateRequest());
        return "boards/write";
    }
    @ResponseBody
    @PostMapping("/rankUpWrite")
    public String rankUpWrite(BoardCreateRequest createRequest, Principal principal){
        boardService.write(createRequest, principal.getName());
        return "redirect:/boards/rankUp/{boardId}";
    }

    // 플래너신청조회
    @GetMapping("/rankUp/{boardId}")
    public String rankUpDetail(@PathVariable Long boardId, Model model){
        BoardDto boardDto = boardService.rankUpDetail(boardId);
        model.addAttribute("board", boardDto);
        return "/boards/rankUpDetail";
    }

    @ResponseBody
    @GetMapping("/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId, Principal principal){
        Long boardDelete = boardService.delete(principal.getName(), boardId);
        log.info("delete");
        return "boards/delete";
    }

    @GetMapping("/list")
    public String listBoard(Pageable pageable, Model model){
        Page<BoardDto> boardPage = boardService.boardList(pageable);
        model.addAttribute("list", boardPage);
        return "boards/list";
    }

    // 포토폴리오 작성
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
            boardId = boardService.writePortfolio(req, file, principal.getName());
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
    public String portfolioDetail(@PathVariable Long boardId, Model model,
                                  @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        BoardDto boardDto = boardService.portfolioDetail(boardId);
        PlannerDetailResponse response = plannerService.findByUser(boardDto.getUserName());

        log.info(boardDto.getTitle());
        model.addAttribute("planner", response);
        model.addAttribute("board", boardDto);

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
        BoardDto boardDto = boardService.portfolioDetail(boardId);
        model.addAttribute("planner", response);
        model.addAttribute(new PortfolioModifyRequest(boardDto.getTitle(), boardDto.getContent(), boardDto.getImage()));
        return "boards/portfolioModify";
    }

    @ResponseBody
    @PostMapping("portfolio/{boardId}/modify")
    public String portfolioModify(@PathVariable Long boardId, @RequestPart(value = "request") PortfolioModifyRequest req,
                                  @RequestPart(value = "file",required = false) MultipartFile file,  Principal principal, Model model) throws IOException {

        log.info(req.getImage());

        try {
            boardService.portfolioModify(req, file, principal.getName(), boardId);
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

        model.addAttribute("boardId", boardId);
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
}
