package com.example.airplanning.domain.dto.planner;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PlannerEditRequest {

    private Long plannerId;
    private String description;
    private Long regionId;

    public PlannerEditRequest(Long plannerId, String description) {
        this.plannerId = plannerId;
        this.description = description;
    }
}
