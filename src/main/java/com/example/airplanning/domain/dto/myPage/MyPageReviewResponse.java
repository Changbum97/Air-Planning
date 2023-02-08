package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Review;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageReviewResponse {

    private Long id; //해당 리뷰 id
    private String title; //해당 리뷰 제목
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
    private String plannerName; // 리뷰한 플레너 닉네임

    public static MyPageReviewResponse Of(Review review) {

        String titleSub;

        if (review.getTitle().length() <= 10) {
            titleSub = review.getTitle();
        } else {
            titleSub = review.getTitle().substring(0,10)+" ...";
        }

        return MyPageReviewResponse.builder()
                .id(review.getId())
                .title(titleSub)
                .plannerName(review.getPlanner().getUser().getNickname())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static MyPageReviewResponse Of(Like like) {
        return MyPageReviewResponse.builder()
                .id(like.getReview().getId())
                .title(like.getReview().getTitle())
                .plannerName(like.getReview().getPlanner().getUser().getNickname())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
