package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserJoinRequest {

    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID

    private String password;    // 비밀번호
    private String phoneNumber; // 전화번호 01012345678

    private String code; //이메일 인증코드

    public User toEntity(String encodedPassword){
        return User.builder()
                .nickname(this.nickname)
                .email(this.email)
                .userName(this.userName)
                .password(encodedPassword)
                .phoneNumber(this.phoneNumber)
                .role(UserRole.USER)
                .build();
    }
}
