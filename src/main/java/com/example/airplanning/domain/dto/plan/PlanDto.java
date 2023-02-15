package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {

    private Long id;
    private String title;
    private String content;
    private String userName;
    private String nickname;
    private PlanType planType;
    private UserRole userRole;
    private String createdAt;

    private String plannerName;

    private Long userId;
    private Long plannerId;
    private Long plannerUserId;

    public static PlanDto of(Plan plan){
        return PlanDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .content(plan.getContent())
                .userName(plan.getUser().getUserName())
                .nickname(plan.getUser().getNickname())
                .planType(plan.getPlanType())
                .userRole(plan.getUser().getRole())
                .createdAt(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .plannerName(plan.getPlanner().getUser().getNickname())
                .userId(plan.getUser().getId())
                .plannerId(plan.getPlanner().getId())
                .plannerUserId(plan.getPlanner().getUser().getId())
                .build();
    }

    public static Page<PlanDto> planDto(Page<Plan> planPage){
        Page<PlanDto> planDtos = planPage.map(plan -> PlanDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .content(plan.getContent())
                .nickname(plan.getUser().getNickname())
                .planType(plan.getPlanType())
                .createdAt(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .plannerName(plan.getPlanner().getUser().getNickname())
                .build());
        return planDtos;
    }
}
