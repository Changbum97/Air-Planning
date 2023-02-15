package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.enum_class.PointStatus;
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
public class PointDto {

    private Long id;
    private Integer amount;
    private String nickName;
    private PointStatus pointStatus;
    private String impUid;
    private String createdAt;

    public static PointDto of(Point point){
        return PointDto.builder()
                .id(point.getId())
                .amount(point.getAmount())
                .nickName(point.getUser().getNickname())
                .pointStatus(point.getPointStatus())
                .impUid(point.getImpUid())
                .createdAt(point.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }

}
