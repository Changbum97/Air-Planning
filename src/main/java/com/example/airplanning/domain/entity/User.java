package com.example.airplanning.domain.entity;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.UserDto;
import com.example.airplanning.domain.enum_class.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

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
    private String image;       // 프로필 이미지 URL
    private Integer point;      // 포인트

    private String provider;  // 어떤 소셜 로그인 이용했는지 (구글/네이버)
    private String providerId; // 소셜 로그인에 사용한 id

    @Enumerated(EnumType.STRING)
    private UserRole role;      // 권한 (USER, ADMIN, BLACKLIST, PLANNER)

    @OneToMany(mappedBy = "user")
    private List<Board> boards;

    @OneToOne(mappedBy = "user")
    private Planner planner;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    @OneToMany(mappedBy = "user")
    private List<Like> likes;

    @OneToMany(mappedBy = "user")
    private List<Alarm> alarms;
}
