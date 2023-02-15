package com.example.airplanning.domain.dto.review;

import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewCreateRequest {

    private Integer star;       // 별점
    private Long planId;
    private String title;       // 리뷰 제목
    private String content;     // 리뷰 내용

    public ReviewCreateRequest(Long planId) {
        this.planId = planId;
    }

    public Review toEntity(User user, Planner planner, String image) {
        return Review.builder()
                .user(user)
                .planner(planner)
                .title(title)
                .content(content)
                .star(star)
                .image(image)
                .views(0)
                .build();
    }
}
