package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageReviewResponse {

    private Long id; //해당 리뷰 id
    private String title; //해당 리뷰 제목

    public static MyPageReviewResponse Of(Review review) {
        return MyPageReviewResponse.builder()
                .id(review.getId())
                .title(review.getTitle())
                .build();
    }

    public static MyPageReviewResponse Of(Like like) {
        return MyPageReviewResponse.builder()
                .id(like.getReview().getId())
                .title(like.getReview().getTitle())
                .build();
    }
}
