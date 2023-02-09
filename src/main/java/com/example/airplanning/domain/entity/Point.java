package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.PointStatus;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;  //충전한 금액

    // 주문 상태(결제 완료, 결제 완료)
    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;

    private String impUid; // 아임포트 결제 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Point createPoint(User user, Integer amount, String impUid){
        return Point.builder()
                .user(user)
                .amount(amount)
                .pointStatus(PointStatus.POINT_COMPLETE)
                .impUid(impUid)
                .build();
    }

    public void paymentStatusChange(PointStatus pointStatus) {
        if (pointStatus.equals(PointStatus.POINT_CANCEL)) {
            throw new AppException(ErrorCode.INVALID_POINT);
        }
        this.pointStatus = PointStatus.POINT_CANCEL;
    }
}
