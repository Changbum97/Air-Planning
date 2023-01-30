package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {

    private Long id;
    private String title;
    private String content;
    private String userName;
    private PlanType planType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    private String plannerName;

    public static PlanDto of(Plan plan){
        return PlanDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .content(plan.getContent())
                .userName(plan.getUser().getUserName())
                .planType(plan.getPlanType())
                .createdAt(plan.getCreatedAt())
                .plannerName("test_planner")
                .build();
    }

}
