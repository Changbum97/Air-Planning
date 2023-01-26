package com.example.airplanning.domain.dto.review;

import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ReviewDto {

    private Integer star;       // 별점
    private String title;       // 리뷰 제목
    private String content;     // 리뷰 내용
    private LocalDateTime createdAt;
    private String userName;
    private String plannerName;

    public static ReviewDto of(Review review) {
        return ReviewDto.builder()
                .star(review.getStar())
                .title(review.getTitle())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .userName(review.getUser().getUserName())
                .plannerName("플래너123")
                .build();
    }
}
