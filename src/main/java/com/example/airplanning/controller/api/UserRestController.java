package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.user.*;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

    private final UserService userService;

    private final EmailService emailService;

    // 회원가입
    @PostMapping("/join")
    @ApiOperation(value = "회원 가입", notes = "회원 가입을 합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nickname", value = "닉네임"),
            @ApiImplicitParam(name = "email", value = "이메일"),
            @ApiImplicitParam(name = "userName", value = "로그인 시 사용할 아이디"),
            @ApiImplicitParam(name = "password", value = "비밀번호"),
            @ApiImplicitParam(name = "code", value = "이메일 인증 코드(생략 가능)")})
    public Response<UserJoinResponse> join (UserJoinRequest request) {
        UserDto userDto = userService.join(request);
        return Response.success(UserJoinResponse.of(userDto));
    }

    // 회원가입시 아이디 중복체크
    @GetMapping("/check-username")
    @ApiOperation(value = "아이디 중복 체크", notes = "아이디의 중복 여부를 확인합니다.")
    @ApiImplicitParam(name = "username", value = "아이디")
    public Response<Boolean> checkUserName(@RequestParam String username) {
        return Response.success(userService.checkUserName(username));
    }

    // 회원가입시 닉네임 중복 체크
    @GetMapping("/check-nickname")
    @ApiOperation(value = "닉네임 중복 체크", notes = "닉네임의 중복 여부를 확인합니다.")
    @ApiImplicitParam(name = "nickname", value = "닉네임")
    public Response<Boolean> checkNickname(@RequestParam String nickname) {
        System.out.println("==================");
        System.out.println("nickname : " + nickname);
        System.out.println(userService.checkNickname(nickname));
        System.out.println("==================");
        return Response.success(userService.checkNickname(nickname));
    }

    // 회원가입시 이메일 중복 체크
    @GetMapping("/check-email")
    @ApiOperation(value = "이메일 중복 체크", notes = "이메일의 중복 여부를 확인합니다.")
    @ApiImplicitParam(name = "email", value = "이메일")
    public Response<Boolean> checkEmail(@RequestParam String email) {
        return Response.success(userService.checkEmail(email));
    }

    // 인증 이메일 보내기
    @GetMapping("/send-auth-email")
    @ApiOperation(value = "인증 이메일 보내기", notes = "이메일 인증을 위한 인증 이메일을 보냅니다.")
    @ApiImplicitParam(name = "email", value = "이메일")
    public Response<String> sendAuthEmail(@RequestParam String email) throws Exception {
        return Response.success(emailService.sendLoginAuthMessage(email));
    }

    // 인증 이메일 확인하기
    @GetMapping("/check-auth-email")
    @ApiOperation(value = "이메일 인증 코드 확인", notes = "이메일의 인증 코드를 확인합니다.")
    @ApiImplicitParam(name = "code", value = "인증코드")
    public Response<Boolean> checkAuthEmail(@RequestParam String code) {
        System.out.println(code);
        if (emailService.getData(code) == null) return Response.success(false);
        else return Response.success(true);
    }

    // 이메일로 아이디 찾기
    @GetMapping("/find-id-by-email")
    @ApiOperation(value = "아이디 찾기", notes = "이메일로 아이디를 찾습니다.")
    @ApiImplicitParam(name = "email", value = "이메일")
    public Response<String> findIdByEmail(FindByEmailRequest request) throws Exception {
        return Response.success(emailService.sendFoundIdMessage(request.getEmail()));
    }

    // 이메일로 비밀번호 찾기
    @GetMapping("/find-pw-by-email")
    @ApiOperation(value = "비밀번호 찾기", notes = "아이디와 이메일로 아이디를 찾습니다.")
    @ApiImplicitParams({
    @ApiImplicitParam(name = "email", value = "이메일"),
    @ApiImplicitParam(name = "userName", value = "아이디")})
    public Response<String> findPwByEmail(FindByEmailRequest request) throws Exception {
        return Response.success(emailService.sendFoundPasswordMessage(request.getEmail(), request.getUserName()));
    }

    @PostMapping("/set-nickname")
    @ApiOperation(value = "닉네임 설정", notes = "소셜 가입 시, 웹 사이트에서 사용할 닉네임을 설정 합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nickname", value = "닉네임"),
            @ApiImplicitParam(name = "userName", value = "아이디")})
    public Response<String> setNickname(SetNicknameRequest request) {
        userService.setNickname(request.getUserName(), request.getNickname());
        return Response.success(request.getNickname());
    }

}
