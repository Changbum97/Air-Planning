package com.example.airplanning.service;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.LikeRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    public Boolean checkLike(Long boardId, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return likeRepository.existsByBoardIdAndUserId(boardId, user.getId());
    }

    @Transactional
    public String changeLike(Long boardId, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Optional<Like> optLike = likeRepository.findByBoardIdAndUserId(boardId, user.getId());

        // 좋아요가 없으면 좋아요 추가, 있으면 좋아요 삭제
        if(optLike.isEmpty()) {
            Board board = boardRepository.findById(boardId)
                            .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
            likeRepository.save(Like.builder()
                            .user(user)
                            .likeType(LikeType.BOARD_LIKE)
                            .board(board).build());
            return "좋아요가 추가되었습니다.";
        } else {
            likeRepository.delete(optLike.get());
            return "좋아요가 취소되었습니다.";
        }
    }
}
