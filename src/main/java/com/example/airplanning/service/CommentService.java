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
import com.example.airplanning.repository.CommentRepository;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReviewRepository reviewRepository;

    private final AlarmService alarmService;

    public CommentResponse createComment(CommentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        CommentResponse response = null;

        if (request.getCommentType().equals("board")){
            Board board = boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
            Comment savedComment = commentRepository.save(request.toBoardCommentEntity(user, board));
            response = CommentResponse.of(savedComment);
            alarmService.send(board.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId(), board.getTitle());
        } else {
            Review review = reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
            Comment savedComment = commentRepository.save(request.toReviewCommentEntity(user, review));
            response = CommentResponse.of(savedComment);
            alarmService.send(review.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId(), review.getTitle());
        }

        return response;
    }

    @Transactional
    public CommentResponse updateComment(CommentUpdateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (request.getCommentType().equals("board")) {
            boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
        } else {
            reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
        }

        Comment targetComment = commentRepository.findById(request.getTargetCommentId())
                .orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Comment updatedComment = null;

        if (user.getId() == targetComment.getUser().getId()) {
            targetComment.update(request.getContent());
            updatedComment = commentRepository.save(targetComment);
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        return CommentResponse.of(updatedComment);
    }
    @Transactional
    public Long deleteComment(CommentDeleteRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment targetComment = commentRepository.findById(request.getTargetCommentId())
                .orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Long parentId = -1L;

        if (targetComment.getParent() != null) {
            parentId = targetComment.getParent().getId();
        }

        if (user.getId() == targetComment.getUser().getId() || user.getRole().toString().equals("ADMIN")) {
            if (targetComment.getChildren().isEmpty()) {
                commentRepository.delete(targetComment);
            } else {
                targetComment.deleteUpdate();
                commentRepository.save(targetComment);
            }
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        System.out.println(parentId);
        return parentId;
    }

    public void deleteParent(Long parentId) {
        if (parentId != -1L) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (parentComment.getChildren().isEmpty() && parentComment.getDeletedAt() != null) {
                System.out.println("지운다.");
                commentRepository.delete(parentComment);
            }
        }
    }

    public CommentResponse createCoComment(CoCommentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment parentComment = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Comment Coco = null;

        if (request.getCommentType().equals("board")) {
            Board board = boardRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
            Coco = commentRepository.save(request.toBoardCoCommentEntity(user, board, parentComment));
            // 알람 발송
            alarmService.send(board.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId(), board.getTitle());
            alarmService.send(parentComment.getUser(), AlarmType.COMMENT_ALARM, "/boards/"+board.getId(), board.getTitle());
        } else {
            Review review = reviewRepository.findById(request.getPostId())
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
            Coco = commentRepository.save(request.toReviewCoCommentEntity(user, review, parentComment));
            // 알람 발송
            alarmService.send(review.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId(), review.getTitle());
            alarmService.send(parentComment.getUser(), AlarmType.COMMENT_ALARM, "/reviews/"+review.getId(), review.getTitle());
        }

        return CommentResponse.ofCoco(Coco);
    }

    public Page<CommentResponse> readComment(Long postId, String postType, Pageable pageable) {
        Page<Comment> commentPage = null;

        if (postType.contains("board")) {
            Board board = boardRepository.findById(postId)
                    .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));
            commentPage = commentRepository.findByBoardAndParentIsNull(board, pageable);
        } else {
            Review review = reviewRepository.findById(postId)
                    .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_FOUND));
            commentPage = commentRepository.findByReviewAndParentIsNull(review, pageable);
        }

        return commentPage.map(comment -> CommentResponse.of(comment));
    }
}
