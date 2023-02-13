package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCreateRequest {

    private Long plannerId;
    private String title;
    private String content;
    private PlanType planType;

    public PlanCreateRequest(Long plannerId) {
        this.plannerId = plannerId;
    }

    public Plan toEntity(User user, Planner planner){
        return Plan.builder()
                .user(user)
                .planner(planner)
                .title(title)
                .content(content)
                .planType(PlanType.FINISH)
                .isReviewed(false)
                .build();
    }
}
