package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentDtoWithCoCo;
import com.example.airplanning.domain.dto.comment.CommentUpdateRequest;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentRestController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{postId}/create")
    public Response<CommentDto> createComment (@PathVariable Long postId, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail, CommentCreateRequest request) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        log.info("컨트롤러 접근");
        CommentDto commentDto = commentService.create(postId, 6L, request);
        return Response.success(commentDto);
    }

    // 대댓글 작성
    @PostMapping("/{postId}/create/{parentCommentId}")
    public Response<CommentDto> createCoComment(@PathVariable Long postId, @PathVariable Long parentCommentId, CommentCreateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        CommentDto commentDto = commentService.createCoComment(postId, parentCommentId, 6L, request );
        return Response.success(commentDto);
    }

    // 댓글 하나 조회
    @GetMapping("/{commentId}/read")
    public Response<CommentDto> readComment(@PathVariable Long commentId) {
        CommentDto commentDto = commentService.read(commentId);
        return Response.success(commentDto);
    }

    // 댓글 수정
    @PutMapping("/{commentId}/update")
    public Response<CommentDto> updateComment(@PathVariable Long commentId, CommentUpdateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        CommentDto commentDto = commentService.update(commentId, request, 6L);
        return Response.success(commentDto);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}/delete")
    public Response<String> deleteComment(@PathVariable Long commentId, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        // 로그인 정보 말고, 임시로 6번 유저 댓글로 설정
        String deleteMessage = commentService.delete(commentId, 6L);
        return Response.success(deleteMessage);
    }

    // 게시글 댓글 전체 조회
    @GetMapping("/{postId}/readpage")
    public Response<Page<CommentDto>> readCommentPage (@PathVariable Long postId, String commentType, @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentDto> commentDtoPage = commentService.readPage(postId, commentType, pageable);
        return Response.success(commentDtoPage);
    }

    // 게시글 에서 부모댓글(대댓글 제외)만 조회 + 부모 댓글이 가짓 대댓글까지 조회
    @GetMapping("/{postId}/readparentonly")
    public Response<Page<CommentDtoWithCoCo>> readParentCommentOnly (@PathVariable Long postId, @ApiIgnore @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentDtoWithCoCo> commentPage = commentService.readBoardParentCommentOnly(postId, pageable);
        return Response.success(commentPage);
    }
}
