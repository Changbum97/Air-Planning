package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.UserDto;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    // 로그인 성공 페이지
    @GetMapping("/login")
    public ResponseEntity<UserDto> login(Principal principal) {
        UserDto userDto = userService.findUser(principal.getName());
        return ResponseEntity.ok().body(userDto);
    }

    // 로그인 실패 페이지
    @PostMapping ("/login2")
    public ResponseEntity<Object> login2(HttpServletRequest request) {
        return ResponseEntity.ok().body(request.getAttribute("LoginFailMessage"));
    }

}
