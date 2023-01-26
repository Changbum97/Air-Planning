package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.enum_class.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCreateResponse {

    private String title;
    private String content;
    private PlanType planType;

    public static PlanCreateResponse of(PlanDto plan){
        return PlanCreateResponse.builder()
                .title(plan.getTitle())
                .content(plan.getContent())
                .planType(PlanType.WAITING)
                .build();
    }

}
