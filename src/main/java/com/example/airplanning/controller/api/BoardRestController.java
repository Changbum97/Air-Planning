package com.example.airplanning.controller.api;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardModifyRequest;
import com.example.airplanning.domain.dto.board.BoardModifyResponse;
import com.example.airplanning.domain.dto.board.BoardResponse;
import com.example.airplanning.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
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
    public Response<BoardDto> findById(@PathVariable Long id) {
        BoardDto boardDto =  boardService.detail(id);
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
    @PutMapping("/{boardId}")
    public Response<BoardModifyResponse> update(@PathVariable Long id, @RequestBody BoardModifyRequest boardModifyRequest, Principal principal){
        String userName = principal.getName();
        BoardDto boardDto = boardService.modify(boardModifyRequest, "test", id);
        return Response.success(new BoardModifyResponse("포스트 수정이 완료되었습니다.", boardDto.getId()));
    }




    // 삭제

    // Post 리스트 조회



    // 플래너 신청 등록
    @PostMapping("/rankUpWrite/{boardId}")
    public Response<BoardResponse>rankUpWrite(@RequestBody BoardCreateRequest boardCreateRequest, Principal principal) {
        String userName = principal.getName();
        BoardDto boardDto = boardService.write(boardCreateRequest, "test");
        return Response.success(new BoardResponse("프래너 등급 신청이 완료되었습니다.", boardDto.getId()));
    }

    // 플래너 신청 조회
    @GetMapping("/rankUp/{boardId}")
    @Operation(summary = "플래너 신청 조회")
    public Response<BoardDto> rankUpDetail(@PathVariable Long boardId){
        BoardDto boardDto = boardService.rankUpDetail(boardId);
        return Response.success(boardDto);
    }

}
