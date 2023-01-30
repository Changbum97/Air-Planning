package com.example.airplanning.domain.dto.user;

import com.example.airplanning.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserDto {

    private Long id;
    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID
    private String password;    // 비밀번호
    private String phoneNumber; // 전화번호 01012345678
    private String image;       // 프로필 이미지 URL
    private Integer point;      // 포인트
    private String role;      // 권한 (USER, ADMIN, BLACKLIST, PLANNER)

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .userName(user.getUserName())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .image(user.getImage())
                .point(user.getPoint())
                .role(user.getRole().name())
                .build();
    }
}
