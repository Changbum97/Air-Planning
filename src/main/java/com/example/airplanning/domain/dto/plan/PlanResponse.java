package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private String message;
    private Long planId;

    public static PlanResponse of(Plan plan){
        return PlanResponse.builder()
                .planId(plan.getId())
                .message("플랜 신청 상태가 변경되었습니다.")
                .build();
    }
}