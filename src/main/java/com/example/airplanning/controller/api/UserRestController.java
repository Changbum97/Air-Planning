package com.example.airplanning.controller.api;

import com.example.airplanning.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    // 로그인 성공 페이지
    @GetMapping("/login")
    public String login() {
        return "로그인 성공!!!!!!!";
    }

    // 로그인 실패 페이지
    @GetMapping("/login2")
    public String login2() {
        return "로그인 실패!!!!!!!";
    }

}
