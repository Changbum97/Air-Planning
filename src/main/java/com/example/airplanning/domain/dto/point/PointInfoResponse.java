package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.enum_class.PointStatus;
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
public class PointInfoResponse {

    private Long pointId;
    private Integer amount;
    private PointStatus pointStatus;
    private String impUid;
    private String createdAt;

    public static PointInfoResponse toDto(Point point){
        return PointInfoResponse.builder()
                .pointId(point.getId())
                .amount(point.getAmount())
                .pointStatus(point.getPointStatus())
                .impUid(point.getImpUid())
                .createdAt(point.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }

    public static Page<PointInfoResponse> toList(Page<Point> points){
        Page<PointInfoResponse> infoResponses = points.map(point -> PointInfoResponse.builder()
                .pointId(point.getId())
                .amount(point.getAmount())
                .pointStatus(point.getPointStatus())
                .impUid(point.getImpUid())
                .createdAt(point.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build());
        return infoResponses;
    }

}
