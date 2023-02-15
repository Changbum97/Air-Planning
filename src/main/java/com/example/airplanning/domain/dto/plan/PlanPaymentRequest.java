package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPaymentRequest {
    private Long id;
    private String nickname;
    private String createdAt;
    private Integer point;
    private String plannerName;
    private Integer amount;

    public static PlanPaymentRequest of(Plan plan){
        return PlanPaymentRequest.builder()
                .id(plan.getId())
                .nickname(plan.getUser().getNickname())
                .createdAt(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .point(plan.getUser().getPoint())
                .plannerName(plan.getPlanner().getUser().getNickname())
                .amount(plan.getPlanner().getAmount())
                .build();
    }
}
