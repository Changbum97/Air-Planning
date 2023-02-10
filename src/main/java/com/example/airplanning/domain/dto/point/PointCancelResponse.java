package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointCancelResponse {

    private String message;
    private Long pointId;

    public static PointCancelResponse of(Point point){
        return PointCancelResponse.builder()
                .pointId(point.getId())
                .message("결제가 취소되었습니다.")
                .build();
    }
}
