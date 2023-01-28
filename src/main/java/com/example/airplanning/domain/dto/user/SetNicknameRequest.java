package com.example.airplanning.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class SetNicknameRequest {
    public SetNicknameRequest(String userName) {
        this.userName = userName;
    }

    private String userName;
    private String nickname;
}
