package com.example.airplanning.domain.enum_class;

public enum AlarmType {
    REQUEST_PLAN_ALARM,         // 플래너가 신청을 받았을 때
    RESPONSE_PLAN_ALARM,        // 플래너가 신청을 수락/거부 했을 때
    CHATTING_ALARM,             // 채팅이 왔을 때
    REVIEW_ALARM,               // 리뷰가 달렸을 때
    CHANGE_ROLE_ALARM,          // 등급이 변경 되었을 때
    REQUEST_CHANGE_ROLE_ALARM,  // 등급 변경 신청이 왔을 때
    COMMENT_ALARM               // 리뷰/게시글에 댓글이 달렸을 때
}
