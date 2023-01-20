package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.UserJoinRequest;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
