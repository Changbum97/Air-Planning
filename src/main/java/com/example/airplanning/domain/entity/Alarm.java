package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.AlarmType;
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
public class Alarm extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetUrl;       // 알람 클릭 시 이동할 URL

    private boolean isChecked;  // 알람 확인 여부

    //@Enumerated(EnumType.STRING)
    private AlarmType alarmType;    // 알람 타입 (플래너가 신청 받았을때, 플래너가 신청을 수락/거절 했을 때, 채팅이 왔을 때, 리뷰가 달렸을 때,
                                    //          등급이 변경 되었을 때, 등급 변경이 신청되었을 때, 리뷰/게시글에 댓글이 달렸을 때)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 알람을 받는 유저

}
