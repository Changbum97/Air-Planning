package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.user.UserJoinRequest;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("userJoinRequest", new UserJoinRequest());
        return "users/join";
    }

    @ResponseBody
    @PostMapping("/join")
    public String join(UserJoinRequest request) {
        userService.join(request);
        return "회원가입 성공";
    }

    // 회원가입 시 사용될 아이디(userName) 중복 체크
    @ResponseBody
    @GetMapping("/check-userName")
    public Boolean checkUserName(@RequestParam String userName) {
        log.info("username : {}", userName);
        return userService.checkUserName(userName);
    }

    // 회원가입 시 사용될 닉네임 중복 체크
    @ResponseBody
    @GetMapping("/check-nickname")
    public Boolean checkNickname(@RequestParam String nickname) {
        return userService.checkNickname(nickname);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "users/login";
    }

    @GetMapping("/testlogin")
    public String testLogin() {
        return "TestLogin";
    }

    // 회원가입 시 사용될 이메일 중복 체크
    @ResponseBody
    @GetMapping("/check-email")
    public Boolean checkEmail(@RequestParam String email) {
        log.info("emailCheck : {}", email);
        return userService.checkEmail(email);
    }

    // 로그인 시 인증 이메일 보내기
    @ResponseBody
    @GetMapping("/ecert/send")
    public String sendAuthEmail(@RequestParam String email) throws Exception {
        return emailService.sendLoginAuthMessage(email);
    }

    // 이메일 인증 번호 확인하기
    @ResponseBody
    @PostMapping("/ecert/check")
    public Boolean checkAuthEmail(@RequestBody String code) {
        if (emailService.getData(code) == null) return false;
        else return true;
    }
}
