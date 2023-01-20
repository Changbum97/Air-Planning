package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageInfoResponse {

    private String name;        // 본명
    private String birth;       // 생년월일 YYYYMMDD
    private String userName;    // 로그인에 사용할 ID

    public static MyPageInfoResponse of(User user) {
        return MyPageInfoResponse.builder()
                .userName(user.getUserName())
                .birth(user.getBirth())
                .name(user.getName())
                .build();
    }
}
