package com.example.airplanning.controller;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.comment.*;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.CommentService2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService2 commentService2;

    // 댓글 작성
    @PostMapping("/create")
    public void createComment(@ModelAttribute CommentCreateRequest2 request, @AuthenticationPrincipal UserDetail userDetail){
        if (userDetail == null) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        } else {
            commentService2.createBoardComment(request, userDetail.getId());
        }
    }

    // 자유게시판 댓글 목록 조회
    @GetMapping("/{boardId}/readBoardComment")
    public ResponseEntity readBoardComment(@PathVariable Long boardId, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> commentPage = commentService2.readBoardComment(boardId, pageable);
        return ResponseEntity.ok().body(commentPage);
    }

    // 리뷰 댓글 목록 조회
    @GetMapping("/{reviewId}/readReviewComment")
    public ResponseEntity readReviewComment(@PathVariable Long reviewId, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> commentPage = commentService2.readReviewComment(reviewId, pageable);
        return ResponseEntity.ok().body(commentPage);
    }

    // 댓글 수정
    @PostMapping("/update")
    public void updateComment(@ModelAttribute CommentUpdateRequest2 request, @AuthenticationPrincipal UserDetail userDetail) {
        commentService2.updateComment(request, userDetail.getId());
    }

    // 댓글 삭제
    @PostMapping("/delete")
    public void deleteComment(@ModelAttribute CommentDeleteRequest2 request, @AuthenticationPrincipal UserDetail userDetail) {
        Long parentId = commentService2.deleteComment(request, userDetail.getId());
        commentService2.deleteParent(parentId);
    }

    // 대댓글 작성
    @PostMapping("/createcoco")
    public void createCoComment(@ModelAttribute CoCommentCreateRequest2 request, @AuthenticationPrincipal UserDetail userDetail) {
        commentService2.createCoComment(request, userDetail.getId());
    }

}
