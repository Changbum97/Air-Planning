package com.example.airplanning.domain.dto.admin;

import com.example.airplanning.domain.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserFoundbyAdmin {
    private String nickname;
    private String role;

    public static UserFoundbyAdmin of (UserDto userDto) {
        return new UserFoundbyAdmin(userDto.getNickname(), userDto.getRole());
    }
}
