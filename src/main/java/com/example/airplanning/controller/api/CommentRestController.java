package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentUpdateRequest;
import com.example.airplanning.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{boardId}/create")
    public ResponseEntity<Response<CommentDto>> createComment (@PathVariable Long boardId, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail, CommentCreateRequest request, String commentType) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        CommentDto commentDto = commentService.create(boardId, 6L, request, commentType);
        return ResponseEntity.ok().body(Response.success(commentDto));
    }

    // 댓글 하나 조회
    @GetMapping("/{commentId}/read")
    public ResponseEntity<Response<CommentDto>> readComment(@PathVariable Long commentId) {
        CommentDto commentDto = commentService.read(commentId);
        return ResponseEntity.ok().body(Response.success(commentDto));
    }

    // 댓글 수정
    @PutMapping("/{commentId}/update")
    public ResponseEntity<Response<CommentDto>> updateComment(@PathVariable Long commentId, CommentUpdateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        CommentDto commentDto = commentService.update(commentId, request, 6L);
        return ResponseEntity.ok().body(Response.success(commentDto));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}/delete")
    public ResponseEntity<Response<String>> deleteComment(@PathVariable Long commentId, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        String deleteMessage = commentService.delete(commentId, 6L);
        return ResponseEntity.ok().body(Response.success(deleteMessage));
    }
}
