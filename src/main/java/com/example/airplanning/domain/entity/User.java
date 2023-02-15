package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class User extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String userName;    // 로그인에 사용할 ID
    private String password;    // 비밀번호
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

    @OneToMany(mappedBy = "user")
    private List<Plan> plans;

    public void updateUser(String password, String nickname, String image) {
        this.password = password;
        this.nickname = nickname;
        this.image = image;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    public void setNickname(String newNickname) {
        this.nickname = newNickname;
    }
    public void setDefaultImage() {this.image = "https://airplanning-bucket.s3.ap-northeast-2.amazonaws.com/default.jpeg";}
    public void changeRank(String role) {
        log.info("{}", role);
        log.info("{}", UserRole.PLANNER.name());
        if (role.equals(UserRole.USER.name())) {
            this.role = UserRole.USER;
        } else if (role.equals(UserRole.ADMIN.name())) {
            this.role = UserRole.ADMIN;
        } else if (role.equals(UserRole.PLANNER.name())) {
            this.role = UserRole.PLANNER;
        } else if (role.equals(UserRole.BLACKLIST.name())) {
            this.role = UserRole.BLACKLIST;
        }
    }

    public void chargingPoint(Integer chargeAmount) {
        this.point += chargeAmount;
    }

    public void updatePoint(Integer point){
        this.point = point;
    }

    public void plusPoint(Integer amount){
        this.point += amount;
    }
}
