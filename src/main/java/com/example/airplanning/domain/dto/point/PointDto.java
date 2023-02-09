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
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {

    private Long id;
    private Integer amount;
    private String userName;
    private PointStatus pointStatus;
    private String impUid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static PointDto of(Point point){
        return PointDto.builder()
                .id(point.getId())
                .amount(point.getAmount())
                .userName(point.getUser().getUserName())
                .pointStatus(point.getPointStatus())
                .impUid(point.getImpUid())
                .createdAt(point.getCreatedAt())
                .build();
    }

}
