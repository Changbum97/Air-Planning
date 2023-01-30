package com.example.airplanning.service;

import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    @Transactional
    public BoardDto write(BoardCreateRequest boardCreateRequest, String username) {

        User user = userRepository.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("게시글 작성 권한이 없습니다."));
        User userEntity = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED, String.format("%s not founded", username)));
        Board savedBoardEntity = boardRepository.save(boardCreateRequest.toEntity(userEntity));

        BoardDto boardDto = BoardDto.builder()
                .id(savedBoardEntity.getId())
                .build();

        return boardDto;
    }

    public BoardDto detail(Long id){
        Board board = boardRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }
}
