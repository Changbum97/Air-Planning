package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
import com.example.airplanning.domain.dto.comment.CommentUpdateRequest;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.CommentRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public CommentDto create (Long boardId, Long userId, CommentCreateRequest request) {
        // 해당 게시글(Board) 존재 유무 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        // 댓글을 단 유저 존재 유무 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        // 댓글 작성
        Comment savedComment = commentRepository.save(request.toBoardCommentEntity(user,board));

        return CommentDto.ofBoard(savedComment);
    }

    public CommentDto read (Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        return CommentDto.ofBoard(comment);
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

            return CommentDto.ofBoard(updatedComment);
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
}
