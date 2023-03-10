package com.example.airplanning.service;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final PlanRepository planRepository;

    //마이페이지 내정보
    public MyPageInfoResponse getMyPageInfo(String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        return MyPageInfoResponse.of(user);
    }

    //마이페이지 내가 쓴 글
    public Page<MyPageBoardResponse> getMyBoard(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Board> boardPages = boardRepository.findAllByUser(user, pageable);

        return new PageImpl<>(boardPages.stream()
                .map(Board -> MyPageBoardResponse.Of(Board))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 쓴 리뷰
    public Page<MyPageReviewResponse> getMyReview(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Review> reviewPages = reviewRepository.findAllByUser(user, pageable);

        return new PageImpl<>(reviewPages.stream()
                .map(Review -> MyPageReviewResponse.Of(Review))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 쓴 댓글
    public Page<MyPageCommentResponse> getMyComment(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Comment> commentPages = commentRepository.findAllByUser(user, pageable);

        return new PageImpl<>(commentPages.stream()
                .map(Comment -> MyPageCommentResponse.Of(Comment))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 좋아요 한 글
    public Page<MyPageBoardResponse> getLikeBoard(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Like> likePages = likeRepository.findAllByUser(user, pageable);

        return new PageImpl<>(likePages.stream()
                .filter(Like -> Like.getLikeType().equals(LikeType.BOARD_LIKE))
                .map(Like -> MyPageBoardResponse.Of(Like))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 좋아요 한 리뷰
    public Page<MyPageReviewResponse> getLikeReview(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Like> likePages = likeRepository.findAllByUser(user, pageable);

        return new PageImpl<>(likePages.stream()
                .filter(Like -> Like.getLikeType().equals(LikeType.REVIEW_LIKE))
                .map(Like -> MyPageReviewResponse.Of(Like))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 좋아요 한 플래너
    public Page<MyPagePlannerResponse> getLikePlanner(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Like> likePages = likeRepository.findAllByUser(user, pageable);

        return new PageImpl<>(likePages.stream()
                .filter(Like -> Like.getLikeType().equals(LikeType.PLANNER_LIKE))
                .map(Like -> MyPagePlannerResponse.of(Like))
                .collect(Collectors.toList()));

    }

    //마이페이지 여행중
    public Page<MyPagePlanResponse> getProgressPlan(Pageable pageable, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Plan> planPages = planRepository.findAllByUser(user, pageable);

        return new PageImpl<>(planPages.stream()
                .filter(Plan -> Plan.getPlanType().equals(PlanType.WAITING) || Plan.getPlanType().equals(PlanType.ACCEPT) ||
                        Plan.getPlanType().equals(PlanType.REFUSE))
                .map(Plan -> MyPagePlanResponse.of(Plan))
                .collect(Collectors.toList()));
    }

    public Page<MyPagePlanResponse> getFinishPlan(Pageable pageable, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Plan> planPages = planRepository.findAllByUser(user, pageable);

        return new PageImpl<>(planPages.stream()
                .filter(Plan -> Plan.getPlanType().equals(PlanType.COMPLETE))
                .map(Plan -> MyPagePlanResponse.of(Plan))
                .collect(Collectors.toList()));
    }

}

