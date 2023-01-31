package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentDto;
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
}
