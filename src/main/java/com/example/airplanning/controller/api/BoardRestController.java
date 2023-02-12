package com.example.airplanning.controller.api;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.board.BoardDto;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@Slf4j
public class BoardRestController {

    private final BoardService boardService;

    // 등록
    @PostMapping("")
    public Response<String> writeBoard(BoardCreateRequest createRequest, Principal principal){
        boardService.write(createRequest, principal.getName());
        return Response.success("글 등록이 완료되었습니다.");
    }

    // 수정
    @PutMapping("/{boardId}")
    public Response<String> update(@PathVariable Long boardId, BoardModifyRequest boardModifyRequest, Principal principal){
        boardService.modify(boardModifyRequest, principal.getName(), boardId);
        return Response.success("글 수정이 완료되었습니다.");
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
    @GetMapping("/rankUp/{boardId}")
    @Operation(summary = "플래너 신청 조회")
    public Response<BoardDto> rankUpDetail(@PathVariable Long boardId){
        BoardDto boardDto = boardService.rankUpDetail(boardId);
        return Response.success(boardDto);
    }

}

