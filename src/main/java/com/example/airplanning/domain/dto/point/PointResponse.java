package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.enum_class.PointStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {

    private Long pointId;
    private Integer amount;
    private PointStatus pointStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
    private String message;

    public static PointResponse of(Point point){
        return PointResponse.builder()
                .pointId(point.getId())
                .amount(point.getAmount())
                .pointStatus(point.getPointStatus())
                .createdAt(point.getCreatedAt())
                .message("포인트 충전이 완료되었습니다. ")
                .build();
    }


}
