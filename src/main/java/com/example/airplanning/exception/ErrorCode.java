package com.example.airplanning.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "User Name이 중복됩니다."),
    DUPLICATED_USER_EMAIL(HttpStatus.CONFLICT, "User Email이 중복됩니다."),
    NOT_NULL_INDEX(HttpStatus.BAD_REQUEST, "값을 입력해 주세요."),

    INVALID_VERIFICATION_CODE(HttpStatus.UNAUTHORIZED, "인증번호가 일치하지 않습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 잘못되었습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "사용자가 권한이 없습니다."),
    BLACKLIST_USER(HttpStatus.UNAUTHORIZED, "블랙리스트는 접근이 불가능합니다."),

    USER_NOT_FOUNDED(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    PLANNER_NOT_FOUNDED(HttpStatus.NOT_FOUND, "플래너를 찾을 수 없습니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 글이 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글이 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰가 없습니다."),
    CHAT_ROOM_NOT_FOUNDED(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUNDED(HttpStatus.NOT_FOUND, "메지를 찾을 수 없습니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB에러"),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드 에러"),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "정상적이지 않은 요청입니다"),
    AlARM_NOT_FOUND(HttpStatus.NOT_FOUND, "알람을 찾을 수 없습니다."),

    INVALID_POINT(HttpStatus.NOT_FOUND,"이미 취소 처리된 거래입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 거래입니다."),
    INVALID_ORDER(HttpStatus.BAD_REQUEST, "결제를 취소하셨습니다."),
    INVALID_ORDER_TOTAL_POINT(HttpStatus.BAD_REQUEST,"잘못된 결제 금액입니다."),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "플랜 신청을 찾을 수 없습니다."),
    INVALID_LACK_OF_POINT(HttpStatus.BAD_REQUEST,"포인트가 부족합니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다")
    ;

    private HttpStatus status;
    private String message;
}
