package com.example.airplanning.service;

import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;


    // 알람 테스트
    private final AlarmService alarmService;

    public Long write(ReviewCreateRequest request, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        log.info("저장 시작");
        Review savedReview = reviewRepository.save(request.toEntity(user));
        log.info("저장 완료");
        alarmService.send(user, AlarmType.REVIEW_ALARM, "/", savedReview.getTitle());
        return savedReview.getId();
    }

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
