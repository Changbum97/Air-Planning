package com.example.airplanning.service;

import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.*;
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
    private final PlannerRepository plannerRepository;
    private final ReviewRepository reviewRepository;

    public Boolean checkLike(Long boardId, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return likeRepository.existsByBoardIdAndUserId(boardId, user.getId());
    }

    public Boolean checkPlannerLike(Long plannerId, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return likeRepository.existsByPlannerIdAndUserId(plannerId, user.getId());
    }

    public Boolean checkReviewLike(Long reviewId, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return likeRepository.existsByReviewIdAndUserId(reviewId, user.getId());
    }

    @Transactional
    public String changeLike(Long id, String userName, LikeType likeType) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (likeType.equals(LikeType.BOARD_LIKE)) {
            Optional<Like> optLike = likeRepository.findByBoardIdAndUserId(id, user.getId());

            // 좋아요가 없으면 좋아요 추가, 있으면 좋아요 삭제
            if(optLike.isEmpty()) {
                Board board = boardRepository.findById(id)
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

        } else if (likeType.equals(LikeType.PLANNER_LIKE)) {
            Optional<Like> optLike = likeRepository.findByPlannerIdAndUserId(id, user.getId());

            // 좋아요가 없으면 좋아요 추가, 있으면 좋아요 삭제
            if(optLike.isEmpty()) {
                Planner planner = plannerRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.PLANNER_NOT_FOUNDED));
                likeRepository.save(Like.builder()
                        .user(user)
                        .likeType(LikeType.PLANNER_LIKE)
                        .planner(planner).build());
                return "좋아요가 추가되었습니다.";
            } else {
                likeRepository.delete(optLike.get());
                return "좋아요가 취소되었습니다.";
            }
        } else {
            Optional<Like> optLike = likeRepository.findByReviewIdAndUserId(id, user.getId());

            // 좋아요가 없으면 좋아요 추가, 있으면 좋아요 삭제
            if(optLike.isEmpty()) {
                Review review = reviewRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
                likeRepository.save(Like.builder()
                        .user(user)
                        .likeType(LikeType.REVIEW_LIKE)
                        .review(review).build());
                return "좋아요가 추가되었습니다.";
            } else {
                likeRepository.delete(optLike.get());
                return "좋아요가 취소되었습니다.";
            }
        }
    }
}
