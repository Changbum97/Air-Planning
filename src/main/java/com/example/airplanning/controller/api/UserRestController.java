package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.UserDto;
import com.example.airplanning.domain.dto.UserJoinRequest;
import com.example.airplanning.domain.dto.UserJoinResponse;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

    private final UserService userService;

    private final EmailService emailService;

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

    // 회원가입
    @PostMapping("/join")
    public Response<UserJoinResponse> join (@RequestBody UserJoinRequest request) {
        UserDto userDto = userService.join(request);
        return Response.success(UserJoinResponse.of(userDto));
    }

    @PostMapping("/check/username")
    public String checkUserName(String userName) {
        if (!userService.checkUserName(userName)) {
            return "사용 가능한 userName 입니다.";
        } else {
            return "중복된 userName 입니다.";
        }
    }
    @PostMapping("/check/email")
    public String checkEmail(String email) {
        if (!userService.checkEmail(email)) {
            return "사용가능한 email 입니다.";
        } else {
            return "중복된 email 입니다.";
        }
    }

    @PostMapping("/check/phone")
    public String checkPhoneNumber(String phoneNumber) {
        if (!userService.checkPhoneNumber(phoneNumber)) {
            return "사용 가능한 PhoneNumber 입니다.";
        } else {
            return "중복된 PhoneNumber 입니다.";
        }
    }

    // 인증 이메일 보내기
    @PostMapping("/emailcertification")
    public ResponseEntity<String> emailConfirm(
            @RequestBody @ApiParam(value="이메일정보 정보", required = true)String email) throws Exception {
        log.info("email : {} ", email);

        String confirm = emailService.sendSimpleMessage(email);

        return ResponseEntity.ok().body(confirm);
    }

    // 인증 이메일 확인하기
    @PostMapping("/emailcheck")
    public String emailConfirm2(@RequestBody String code) {
        log.info("email : {} ", code);

        if (emailService.getData(code)==null) {
            return "인증실패!";
        } else {
            return "인증성공!";
        }
    }

}
