package com.example.airplanning.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class AppException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    @Override
    public String toString() {

        //exception 발생 시 메세지를 입력하지 않으면, ErrorCode.enum의 message를 따른다.
        if(message.equals("")) {
            return errorCode.getMessage();
        }

        //메세지를 입력하면, ErrorCode.enum의 message + 입력한 message로 exception message 출력
        return String.format("%s, %s", errorCode.getMessage(), message);
    }
}
