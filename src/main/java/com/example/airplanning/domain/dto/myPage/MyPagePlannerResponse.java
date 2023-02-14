package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
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
public class MyPagePlannerResponse {

    private Long id; // 플래너 id
    private String plannerName; // 플래너 닉네임
    private String starMean;        // 별점 평균
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static MyPagePlannerResponse of(Like like) {

        String plannerName ="";
        if (like.getPlanner().getUser().getNickname().length() <= 10) {
            plannerName = like.getPlanner().getUser().getNickname();
        } else {
            plannerName = like.getPlanner().getUser().getNickname().substring(0,10)+"...";
        }

        return MyPagePlannerResponse.builder()
                .id(like.getPlanner().getId())
                .plannerName(plannerName)
                .starMean(String.format("%.2f", (double)like.getPlanner().getStarSum()/(double)like.getPlanner().getReviewCount()))
                .createdAt(like.getCreatedAt())
                .build();
    }
}
