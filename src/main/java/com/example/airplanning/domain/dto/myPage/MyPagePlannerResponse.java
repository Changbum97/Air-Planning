package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPagePlannerResponse {

    private Long id; // 플래너 id
    private String userName; //이름 (닉네임으로 변경될 듯!!!)
    private Integer reviewCount;    // 리뷰 개수
    private Integer starSum;        // 별점 총 합
    private String country;         // 자신있는 국가
    private String region;          // 자신있는 지역

    public static MyPagePlannerResponse of(Like like) {
        return MyPagePlannerResponse.builder()
                .id(like.getPlanner().getId())
                .userName(like.getPlanner().getUser().getUserName())
                .reviewCount(like.getPlanner().getReviewCount())
                .starSum(like.getPlanner().getStarSum())
                .country(like.getPlanner().getCountry())
                .region(like.getPlanner().getRegion())
                .build();
    }
}
