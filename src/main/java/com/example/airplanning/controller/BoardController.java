package com.example.airplanning.controller;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardModifyRequest;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentDtoWithCoCo;
import com.example.airplanning.domain.dto.plan.PlanUpdateRequest;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.service.BoardService;
import com.example.airplanning.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

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
    public String detailBoard(@PathVariable Long boardId, Model model, @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        BoardDto boardDto = boardService.detail(boardId);
        model.addAttribute("board", boardDto);

        Page<CommentDtoWithCoCo> commentPage = commentService.readBoardParentCommentOnly(boardId, pageable);
        Page<CommentDto> commentSize = commentService.readPage(boardId, "BOARD_COMMENT", pageable);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("commentCreateRequest", new CommentCreateRequest());
        model.addAttribute("commentSize", commentSize.getTotalElements());
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
}