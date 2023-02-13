package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.domain.enum_class.PointStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointUsedResponse {

    private Long pointId;
    private Integer amount;
    private PlanType planType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
    private String message;

    public static PointUsedResponse of(Point point, Plan plan){
        return PointUsedResponse.builder()
                .pointId(point.getId())
                .amount(point.getAmount())
                .planType(plan.getPlanType())
                .createdAt(point.getCreatedAt())
                .message("포인트 사용이 완료되었습니다. ")
                .build();
    }


}


