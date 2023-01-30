package com.example.airplanning.domain.dto.planner;

import com.example.airplanning.domain.entity.Planner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PlannerDetailResponse {

    private String userName;
    private Double star;            // 별점 총 합 / 리뷰 개수
    private String country;         // 자신있는 국가
    private String region;          // 자신있는 지역
    private String description;     // 자기 소개

    public static PlannerDetailResponse of(Planner planner) {
        return PlannerDetailResponse.builder()
                .userName(planner.getUser().getUserName())
                .star((double)planner.getStarSum() / planner.getReviewCount())
                .country(planner.getCountry())
                .region(planner.getRegion())
                .description(planner.getDescription())
                .build();
    }
}
