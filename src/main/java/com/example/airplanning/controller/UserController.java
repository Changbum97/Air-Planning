package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.user.FindByEmailRequest;
import com.example.airplanning.domain.dto.user.SetNicknameRequest;
import com.example.airplanning.domain.dto.user.UserJoinRequest;
import com.example.airplanning.domain.dto.user.UserLoginRequest;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


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
    public String loginPage(Model model) {
        model.addAttribute("userLoginRequest", new UserLoginRequest());
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
    @GetMapping("/ecert/check")
    public Boolean checkAuthEmail(@RequestParam String code) {
        System.out.println(code);
        if (emailService.getData(code) == null) return false;
        else return true;
    }

    @GetMapping("/find-id")
    public String findIdPage(Model model) {
        model.addAttribute("findByEmailRequest", new FindByEmailRequest());
        return "users/find-id";
    }

    @GetMapping("/find-pw")
    public String findPwPage(Model model) {
        model.addAttribute("findByEmailRequest", new FindByEmailRequest());
        return "users/find-pw";
    }

    @ResponseBody
    @GetMapping("/find-id-by-email")
    public String findIdByEmail(FindByEmailRequest request) {
        String message = "메일로 아이디를 전송했습니다";

        try {
            String email = request.getEmail();
            String userName = userService.findIdByEmail(email);
            emailService.sendFoundIdMessage(email, userName);
        } catch (AppException e) {
            message = e.getMessage();
        } catch (Exception e) {
            message = "메일 전송에 실패하였습니다.";
        }

        return message;
    }

    @ResponseBody
    @GetMapping("/find-pw-by-email")
    public String findPwByEmail(FindByEmailRequest request) {
        String message = "메일로 새로운 비밀번호를 전송했습니다";

        try {
            String email = request.getEmail();
            String userName = userService.findIdByEmail(email);
            if(!userName.equals(request.getUserName())) {
                message = "아이디에 해당하는 이메일이 일치하지 않습니다";
            } else {
                String newPassword = emailService.sendFoundPasswordMessage(email);
                userService.changePassword(userName, newPassword);
            }
        } catch (AppException e) {
            message = e.getMessage();
        } catch (Exception e) {
            message = "메일 전송에 실패하였습니다.";
        }

        return message;
    }

    // 소셜 로그인으로 가입한 유저가 닉네임을 설정하는 페이지
    @GetMapping("/set-nickname")
    public String setNicknamePage(Principal principal, Model model) {
        model.addAttribute("setNicknameRequest", new SetNicknameRequest(principal.getName()));
        return "users/set-nickname";
    }

    // 소셜 로그인 유저 닉네임 등록
    @ResponseBody
    @PostMapping("/nickname")
    public String setNickname(SetNicknameRequest request) {
        userService.setNickname(request.getUserName(), request.getNickname());
        return request.getNickname();
    }
}
