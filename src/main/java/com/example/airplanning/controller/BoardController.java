package com.example.airplanning.controller;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;

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
    public String detailBoard(@PathVariable Long boardId, Model model){
        BoardDto boardDto = boardService.detail(boardId);
        model.addAttribute("board", boardDto);
        return "boards/detail";
    }

}