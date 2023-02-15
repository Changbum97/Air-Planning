package com.example.airplanning.domain.dto.plan;

import com.example.airplanning.domain.entity.Plan;
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
public class PlanListResponse {

    private Long id;
    private String title;
    private String nickname;
    private String createdAt;
    private String plannerName;

    public static PlanListResponse of(Plan plan){
        String title = plan.getTitle();
        if (title.length() > 10){
            title = title.substring(0, 10) + "...";
        }
        String nickname = plan.getUser().getNickname();
        if (nickname.length() > 10){
            nickname = nickname.substring(0, 10) + "...";
        }

        return PlanListResponse.builder()
                .id(plan.getId())
                .title(title)
                .nickname(nickname)
                .createdAt(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .plannerName(plan.getPlanner().getUser().getNickname())
                .build();
    }

    public static Page<PlanListResponse> toDtoList(Page<Plan> plans){
        return plans.map(plan -> PlanListResponse.of(plan));
    }

}
