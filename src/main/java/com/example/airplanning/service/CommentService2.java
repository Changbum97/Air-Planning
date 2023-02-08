package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
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

    private final AlarmService alarmService;

    public void createBoardComment(CommentCreateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (request.getCommentType().equals("board")){
            Board board = boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
            commentRepository2.save(request.toBoardCommentEntity(user, board));
            alarmService.send(board.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId());
        } else {
            Review review = reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
            commentRepository2.save(request.toReviewCommentEntity(user, review));
            alarmService.send(review.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId());
        }
    }

    @Transactional
    public void updateComment(CommentUpdateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (request.getCommentType().equals("board")) {
            boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
        } else {
            reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
        }

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
    public Long deleteComment(CommentDeleteRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment targetComment = commentRepository2.findById(request.getTargetCommentId())
                .orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Long parentId = -1L;

        if (targetComment.getParent() != null) {
            parentId = targetComment.getParent().getId();
        }

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

        System.out.println(parentId);
        return parentId;
    }

    public void deleteParent(Long parentId) {
        if (parentId != -1L) {
            Comment parentComment = commentRepository2.findById(parentId)
                    .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (parentComment.getChildren().isEmpty() && parentComment.getDeletedAt() != null) {
                System.out.println("지운다.");
                commentRepository2.delete(parentComment);
            }
        }
    }

    public void createCoComment(CoCommentCreateRequest2 request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment parentComment = commentRepository2.findById(request.getParentId())
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (request.getCommentType().equals("board")) {
            Board board = boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
            commentRepository2.save(request.toBoardCoCommentEntity(user, board, parentComment));
            // 알람 발송
            alarmService.send(board.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId());
            alarmService.send(parentComment.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId());
        } else {
            Review review = reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
            commentRepository2.save(request.toReviewCoCommentEntity(user, review, parentComment));
            // 알람 발송
            alarmService.send(review.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId());
            alarmService.send(parentComment.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId());
        }
    }

    public Page<CommentResponse> readBoardComment(Long boardId, Pageable pageable) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
        Page<Comment> commentPage = commentRepository2.findByBoardAndParentIsNull(board, pageable);
        return commentPage.map(comment -> CommentResponse.of(comment));
    }

    public Page<CommentResponse> readReviewComment(Long reviewId, Pageable pageable) {
       Review review = reviewRepository.findById(reviewId)
               .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
        Page<Comment> commentPage = commentRepository2.findByReviewAndParentIsNull(review, pageable);
        return commentPage.map(comment -> CommentResponse.of(comment));
    }
}
