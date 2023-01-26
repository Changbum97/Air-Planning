package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCreateRequest {

    private String title;
    private String content;
    private PlanType planType;

    public Plan toEntity(User user){
        return Plan.builder()
                .user(user)
                .title(title)
                .content(content)
                .planType(PlanType.WAITING)
                .build();
    }
}
