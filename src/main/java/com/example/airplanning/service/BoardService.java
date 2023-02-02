package com.example.airplanning.service;

import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardDeleteRequest;
import com.example.airplanning.domain.dto.board.BoardModifyRequest;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
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

    public BoardDto detail(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }


    // 수정
    public BoardDto modify(BoardModifyRequest modifyRequest, String userName, Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        board.modify(modifyRequest.getTitle(), modifyRequest.getContent());
        boardRepository.save(board);
        return BoardDto.of(board);

    }

    public Board view(Long id){
        return boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
    }
    
    public BoardDto rankUpWrite(BoardCreateRequest boardCreateRequest, String userName) {
        User userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Board board= boardRepository.save(boardCreateRequest.toEntity(userEntity));

        return BoardDto.of(board);
    }


    // 플래너신청조회
    public BoardDto rankUpDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }
    
    // 삭제
    @Transactional
    public Long delete(String userName, Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (!Objects.equals(board.getUser().getUserName(),userName)){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        boardRepository.deleteById(id);
        return id;


    }

}