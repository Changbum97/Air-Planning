package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.PlanType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPagePlanResponse {

    private Long id; //플랜 id
    private String title; //플랜 제목
    private Long plannerId; //플래너 id
    private PlanType planType;  //신청 수락 여부(신청 중, 신청 수락, 신청 거절, 여행 완료)
    private boolean isReviewed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static MyPagePlanResponse of(Plan plan) {

        String titleSub;

        if (plan.getTitle().length() <= 10) {
            titleSub = plan.getTitle();
        } else {
            titleSub = plan.getTitle().substring(0,10)+" ...";
        }

        return MyPagePlanResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .plannerId(plan.getPlanner().getId())
                .planType(plan.getPlanType())
                .isReviewed(plan.isReviewed())
                .createdAt(plan.getCreatedAt())
                .build();
    }

}
