package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPagePlanResponse {

    private Long id; //플랜 id
    private String title; //플랜 제목
    private Long plannerId; //플래너 id
    private PlanType planType;  //신청 수락 여부(신청 중, 신청 수락, 신청 거절, 여행 완료)

    public static MyPagePlanResponse of(Plan plan) {
        return MyPagePlanResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .plannerId(plan.getPlanner().getId())
                .planType(plan.getPlanType())
                .build();
    }

}
