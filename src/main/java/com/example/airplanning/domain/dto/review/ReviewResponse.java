package com.example.airplanning.domain.dto.review;

import com.example.airplanning.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private String plannerNickName;
    private String userNickName; //작성자 닉네임
    private String title;       // 리뷰 제목
    private String content;     // 리뷰 내용
    private String createdAt;
    private String updatedAt;
    private Integer star;


    public static ReviewResponse of (Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .plannerNickName(review.getPlanner().getUser().getNickname())
                .userNickName(review.getUser().getNickname())
                .title(review.getTitle())
                .content(review.getContent())
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .updatedAt(review.getUpdatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .star(review.getStar())
                .build();
    }
}
