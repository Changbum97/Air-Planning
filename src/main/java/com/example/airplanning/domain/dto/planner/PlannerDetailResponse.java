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

    private Long id;
    private String userName;
    private String image;
    private Double star;            // 별점 총 합 / 리뷰 개수
    private String region;          // 자신있는 지역
    private String description;     // 자기 소개

    public static PlannerDetailResponse of(Planner planner) {
        return PlannerDetailResponse.builder()
                .id(planner.getId())
                .userName(planner.getUser().getUserName())
                .image(planner.getUser().getImage())
                .star((double)planner.getStarSum() / planner.getReviewCount())
                .region(planner.getRegion().getRegion1() +" "+ planner.getRegion().getRegion2())
                .description(planner.getDescription())
                .build();
    }
}
