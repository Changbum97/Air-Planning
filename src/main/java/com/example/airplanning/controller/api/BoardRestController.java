package com.example.airplanning.controller.api;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import springfox.documentation.annotations.ApiIgnore;

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
    @PostMapping("/{boardid}/write")
    public Response<BoardResponse>write(@RequestBody BoardCreateRequest boardCreateRequest, Principal principal) {
        String userName = principal.getName();
        BoardDto boardDto = boardService.write(boardCreateRequest, "test");
        return Response.success(new BoardResponse("포스트 등록이 완료되었습니다.", boardDto.getId()));
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



    // Post 리스트

}
