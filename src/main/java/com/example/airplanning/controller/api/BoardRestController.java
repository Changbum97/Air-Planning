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
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
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
    @PostMapping("")
    public Response<String> writeBoard(BoardCreateRequest createRequest, Principal principal){
        boardService.write(createRequest, principal.getName());
        return Response.success("글 등록에 성공했습니다.");
    }


    // 수정
    @PutMapping("/{boardId}/modify")
    public Response<BoardResponse> update(@PathVariable Long id, @RequestBody BoardModifyRequest boardModifyRequest, Principal principal){
        String userName = principal.getName();
        BoardDto boardDto = boardService.modify(boardModifyRequest, "test", id);
        return Response.success(new BoardResponse("포스트 수정이 완료되었습니다.", boardDto.getId()));
    }


    // 삭제
    @DeleteMapping("/{boardid}/delete")
    public Response<BoardResponse> delete(@PathVariable Long id, Principal principal) {
        String userName = principal.getName();
        Long boardDelete = boardService.delete("test", id);
        return Response.success(new BoardResponse("포스트 삭제가 완료되었습니다.", boardDelete));
    }

    // 상세
    @DeleteMapping("/{boardid}/detail")
    public Response<BoardDto> detail(@PathVariable Long id, Principal principal) {
        String userName = principal.getName();
       BoardDto boardDto = boardService.detail(id);
        return Response.success(boardDto);
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

