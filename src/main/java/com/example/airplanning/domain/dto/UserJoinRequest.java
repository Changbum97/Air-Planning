package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserJoinRequest {
    private String name;        // 본명
    private String birth;       // 생년월일 YYYYMMDD
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID
    private String password;    // 비밀번호
    private String phoneNumber; // 전화번호 01012345678
    private String image;       // 프로필 이미지 URL

    public User toEntity(String password){
        return User.builder()
                .name(this.name)
                .birth(this.birth)
                .email(this.email)
                .userName(this.userName)
                .password(password)
                .phoneNumber(this.phoneNumber)
                .image(this.image)
                .role(UserRole.USER)
                .build();
    }
}
