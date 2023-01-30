package com.example.airplanning.domain.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlanUpdateRequest {

    private String title;
    private String content;
}