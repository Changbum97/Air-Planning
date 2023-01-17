package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 본명
    private String birth;       // 생년월일 YYYYMMDD
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID
    private String password;    // 비밀번호
    private String phoneNumber; // 전화번호 01012345678
    private UserRole role;      // 권한 (USER, ADMIN, BLACKLIST, PLANNER)
    private String image;       // 프로필 이미지 URL
    private Integer point;      // 포인트
}
