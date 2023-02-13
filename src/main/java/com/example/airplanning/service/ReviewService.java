package com.example.airplanning.service;

import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.dto.review.ReviewListResponse;
import com.example.airplanning.domain.dto.review.ReviewUpdateRequest;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.PlannerRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final PlannerRepository plannerRepository;

    private final PlanRepository planRepository;


    // 알람 테스트
    private final AlarmService alarmService;

    public Page<ReviewListResponse> reviewList(Pageable pageable, String searchType, String keyword) {
        Page<Review> reviews;

        if (searchType == null) {
            reviews = reviewRepository.findAll(pageable);
        } else {
            if (searchType.equals("TITLE")) {
                reviews = reviewRepository.findAllByTitleContains(keyword, pageable);
            } else {
                reviews = reviewRepository.findAllByPlannerUserNicknameContains(keyword, pageable);
            }
        }
        return reviews.map(review -> ReviewListResponse.of(review));
    }


    @Transactional
    public Long write(ReviewCreateRequest request, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        Review savedReview = reviewRepository.save(request.toEntity(user, plan.getPlanner()));

        plan.reviewedPlan();
        planRepository.save(plan);

        alarmService.send(plan.getPlanner().getUser(), AlarmType.REVIEW_ALARM, "/reviews/"+savedReview.getId(), savedReview.getTitle());
        return savedReview.getId();
    }

    @Transactional
    public Long update(Long reviewId, ReviewUpdateRequest request, String userName) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!userName.equals(review.getUser().getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        review.update(request);
        Review updatedReview = reviewRepository.save(review);
        return updatedReview.getId();
    }

    @Transactional
    public void delete(Long reviewId, String userName) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!userName.equals(review.getUser().getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        reviewRepository.delete(review);
    }

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
