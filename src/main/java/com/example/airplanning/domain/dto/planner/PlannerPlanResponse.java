package com.example.airplanning.domain.dto.planner;

import com.example.airplanning.domain.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerPlanResponse {
    private Long id;
    private String title;
    private String userName;
    private String planType;
    private String createdAt;

    public static PlannerPlanResponse of (Plan plan) {
        return PlannerPlanResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .userName(plan.getUser().getUserName())
                .planType(plan.getPlanType().name())
                .createdAt(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }
}
