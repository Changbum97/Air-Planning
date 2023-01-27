package com.example.airplanning.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindPasswordRequest {
    private String userName;
    private String email;
}
