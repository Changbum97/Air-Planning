package com.example.airplanning.domain.enum_class;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlarmType {

    REQUEST_PLAN_ALARM("플랜 요청이 들어왔습니다."),         // 플래너가 신청을 받았을 때
    ACCEPTED_PLAN_ALARM("플랜 요청이 수락되었습니다."),        // 플래너가 신청을 수락
    REFUSED_PLAN_ALARM("플랜 요청이 거절되었습니다."),          // 플래너가 신청을 거절
    CHATTING_ALARM("메시지가 왔습니다."),             // 채팅이 왔을 때
    REVIEW_ALARM("리뷰가 작성되었습니다."),               // 리뷰가 달렸을 때
    CHANGE_ROLE_ALARM("등급이 변경되었습니다."),          // 등급이 변경 되었을 때
    REQUEST_CHANGE_ROLE_ALARM("등급 변경 신청이 발생했습니다."),  // 등급 변경 신청이 왔을 때
    COMMENT_ALARM("댓글이 달렸습니다.");          // 리뷰/게시글에 댓글이 달렸을 때

    private String message;
}
