package com.example.airplanning.controller.api;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardResponse;
import com.example.airplanning.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
@Slf4j

public class BoardRestController {

    private final BoardService boardService;

    // Post 1개 조회
    @GetMapping("/{boardid}")
    public Response<BoardDto> findById(@PathVariable Long boardid) {
        BoardDto boardDto =  boardService.detail(boardid);
        return Response.success(boardDto);
    }


    // 등록
    @PostMapping("/{boardid}")
    public Response<BoardResponse>write(@RequestBody BoardCreateRequest boardCreateRequest, Principal principal) {
        String userName = principal.getName();
        BoardDto boardDto = boardService.write(boardCreateRequest, "test");
        return Response.success(new BoardResponse("포스트 등록이 완료되었습니다.", boardDto.getId()));
    }

    // 수정

    // 삭제

    // Post 리스트 조회

}
