package com.example.airplanning.domain.dto.review;

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
    private String title;       // 리뷰 제목
    private String content;     // 리뷰 내용

    public Review toEntity(User user) {
        return Review.builder()
                .user(user)
                .title(title)
                .content(content)
                .star(star)
                .build();
    }
}
