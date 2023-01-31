package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentUpdateRequest;
import com.example.airplanning.domain.dto.myPage.MyPageCommentResponse;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.CommentType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.CommentRepository;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public CommentDto create (Long postId, Long userId, CommentCreateRequest request) {
        // 댓글을 단 유저 존재 유무 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Comment savedComment = null;
        // Board 혹은 Review 가 존재하는 지 확인
        if (request.getCommentType().equals(CommentType.BOARD_COMMENT.name())) {
            Board board = boardRepository.findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
            savedComment = commentRepository.save(request.toBoardCommentEntity(user, board));
        } else {
            Review review = reviewRepository.findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
            savedComment = commentRepository.save(request.toReviewCommentEntity(user, review));
        }

        return CommentDto.of(savedComment);
    }

    public CommentDto read (Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        return CommentDto.of(comment);
    }

    @Transactional
    public CommentDto update (Long commentId, CommentUpdateRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (comment.getUser().getId() == userId) {
            comment.update(request);
            Comment updatedComment = commentRepository.save(comment);

            return CommentDto.of(updatedComment);
        } else {
            throw  new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }
    @Transactional
    public String delete (Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (comment.getUser().getId() == userId) {
            commentRepository.delete(comment);
            return "댓글이 삭제되었습니다.";
        } else {
            throw  new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    public Page<CommentDto> readPage(Long postId, String commentType, Pageable pageable) {
        Page<Comment> commentPage = null;

        if (commentType.equals(CommentType.BOARD_COMMENT.name())) {
            Board board = boardRepository.findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
            commentPage = commentRepository.findAllByBoard(board, pageable);
        } else {
            Review review = reviewRepository.findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
            commentPage = commentRepository.findAllByReview(review, pageable);
        }

        return new PageImpl<>(commentPage.stream()
                .map(Comment ->CommentDto.of(Comment))
                .collect(Collectors.toList()));
    }
}
