package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
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
public class MyPagePlannerResponse {

    private Long id; // 플래너 id
    private String plannerName; // 플래너 닉네임
    private Integer reviewCount;    // 리뷰 개수
    private String starMean;        // 별점 평균
    private String country;         // 자신있는 국가
    private String region;          // 자신있는 지역
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static MyPagePlannerResponse of(Like like) {
        return MyPagePlannerResponse.builder()
                .id(like.getPlanner().getId())
                .plannerName(like.getPlanner().getUser().getNickname())
                .reviewCount(like.getPlanner().getReviewCount())
                .starMean(String.format("%.2f", (double)like.getPlanner().getStarSum()/(double)like.getPlanner().getReviewCount()))
                .country(like.getPlanner().getCountry())
                .region(like.getPlanner().getRegion())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
