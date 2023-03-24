package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.user.*;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;


@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@ApiIgnore
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // 로그인 성공 페이지
    @GetMapping("/login-success")
    public ResponseEntity<UserDto> login(Principal principal) {
        UserDto userDto = userService.findUser(principal.getName());
        return ResponseEntity.ok().body(userDto);
    }

    // 로그인 실패 페이지
    @PostMapping ("/login-fail")
    public ResponseEntity<Object> login2(HttpServletRequest request) {
        return ResponseEntity.ok().body(request.getAttribute("LoginFailMessage"));
    }

    // 회원가입 페이지
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("userJoinRequest", new UserJoinRequest());
        return "users/join";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("userLoginRequest", new UserLoginRequest());
        return "users/login";
    }

    // 아이디 찾기 페이지
    @GetMapping("/find-id")
    public String findIdPage(Model model) {
        model.addAttribute("findByEmailRequest", new FindByEmailRequest());
        return "users/find-id";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find-pw")
    public String findPwPage(Model model) {
        model.addAttribute("findByEmailRequest", new FindByEmailRequest());
        return "users/find-pw";
    }

    // 소셜 로그인으로 가입한 유저가 닉네임을 설정하는 페이지
    @GetMapping("/set-nickname")
    public String setNicknamePage(Principal principal, Model model) {
        model.addAttribute("setNicknameRequest", new SetNicknameRequest(principal.getName()));
        return "users/set-nickname";
    }

    // 테스트 로그인 페이지
    @GetMapping("/testlogin")
    public String testLogin() {
        return "TestLogin";
    }
}
