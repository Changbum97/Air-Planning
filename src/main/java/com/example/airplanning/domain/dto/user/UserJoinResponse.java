package com.example.airplanning.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserJoinResponse {
    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID
    private String phoneNumber; // 전화번호 01012345678
    private String image;       // 프로필 이미지 URL
    private String role;      // 권한 (USER, ADMIN, BLACKLIST, PLANNER)

    public static UserJoinResponse of (UserDto userDto) {
        return UserJoinResponse.builder()
                .nickname(userDto.getNickname())
                .email(userDto.getEmail())
                .userName(userDto.getUserName())
                .phoneNumber(userDto.getPhoneNumber())
                .image(userDto.getImage())
                .role(userDto.getRole())
                .build();
    }
}
