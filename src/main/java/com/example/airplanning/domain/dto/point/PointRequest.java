package com.example.airplanning.domain.dto.point;

import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PointStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PointRequest {

    private Integer amount;
    private PointStatus pointStatus;
    private String impUid;

    public Point toEntity(User user) {
        return Point.builder()
                .amount(amount)
                .user(user)
                .pointStatus(PointStatus.POINT_COMPLETE)
                .impUid(impUid)
                .build();
    }

}
