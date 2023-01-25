package com.example.airplanning.controller;


import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.BoardCreateRequest;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.response.BoardResponse;
import com.example.airplanning.service.BoardService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Api(tags = {"Post Controller"})
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    public Response<BoardResponse> posts(@RequestBody BoardCreateRequest dto, Authentication authentication){
        System.out.println("Controller Test Enter");
        BoardDto boardDto = boardService.write(dto, "test");
        System.out.println("Controller Test");
        return Response.success(new BoardResponse("포스트 등록 완료", boardDto.getId()));
    }



//    /* Post 1개 조회
//     */
//    @GetMapping("/{postId}")
//    public ResponseEntity<Response<BoardDto>> findById(@PathVariable Integer postId) {
//        BoardDto postDto = boardService.get(boardId);
//        return ResponseEntity.ok().body(Response.success(postDto));
//    }
//
//
//    /* Post List 조회
//     */
//    @GetMapping
//    public ResponseEntity<Response<Page<BoardDto>>> getPostList(@PageableDefault(size = 20)
//                                                               @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        Page<BoardDto> postDtos = boardService.getAllItems(pageable);
//        return ResponseEntity.ok().body(Response.success(postDtos));
//    }


}