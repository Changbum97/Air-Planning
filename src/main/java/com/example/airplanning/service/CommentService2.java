package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.CommentRepository2;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService2 {

    private final CommentRepository2 commentRepository2;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReviewRepository reviewRepository;

    public CommentResponse createBoardComment(CommentCreateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        Comment createdComment = commentRepository2.save(request.toBoardCommentEntity(user, board));

        return CommentResponse.of2(createdComment);
    }

    @Transactional
    public void updateBoardComment(CommentUpdateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        Comment targetComment = commentRepository2.findById(request.getTargetCommentId())
                .orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (user.getId() == targetComment.getUser().getId()) {
            targetComment.update(request.getContent());
            commentRepository2.save(targetComment);
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    @Transactional
    public void deleteBoardComment(CommentDeleteRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment targetComment = commentRepository2.findById(request.getTargetCommentId())
                .orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (user.getId() == targetComment.getUser().getId()) {
            if (targetComment.getChildren().isEmpty()) {
                commentRepository2.delete(targetComment);
            } else {
                targetComment.deleteUpdate();
                commentRepository2.save(targetComment);
            }

        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    public void createBoardCoComment(CoCommentCreateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        Comment parentComment = commentRepository2.findById(request.getParentId())
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository2.save(request.toBoardCoCommentEntity(user, board, parentComment));
    }

    public Page<CommentResponse> readBoardComment(Long boardId, Pageable pageable) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        Page<Comment> commentPage = commentRepository2.findByBoardAndParentIsNull(board, pageable);

        return commentPage.map(comment -> CommentResponse.of(comment));
    }
}
