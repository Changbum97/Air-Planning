package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.UserDto;
import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/users/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final UserService userService;


    //자신의 마이페이지로 이동
    @ResponseBody
    @GetMapping
    public String toMyPage(Principal principal) {

        String userId = "";

        //principal 오류 (로그인 오류, 만료)
        try {
            userId = Long.toString(userService.findUser(principal.getName()).getId());
        } catch (Exception e) {
            return userId;
        }

        return userId;

    }

    @GetMapping("/{userId}")
    public String myPage(Principal principal, Model model) {

        UserDto user = userService.findUser(principal.getName());

        model.addAttribute("user", user);

        return "users/myPage";

    }

    //마이페이지 비밀번호 확인
    @GetMapping("/{userId}/edit/password")
    public String passwordPage(@PathVariable Long userId, Model model) {

        model.addAttribute("userId", userId);

        return "users/myPagePassword";
    }

    @ResponseBody
    @GetMapping("/{userId}/check-password")
    public String checkPassword(@PathVariable Long userId, @RequestParam String password) {

        try {
            UserDto user = userService.findUserById(userId);
            userService.checkPassword(user.getUserName(), password);
        } catch (AppException e) {
            //user가 찾아지지 않을 때
            if(e.getErrorCode().equals(ErrorCode.USER_NOT_FOUNDED)) {
                return "1";
            }
            // 비밀번호 오류
            else if(e.getErrorCode().equals(ErrorCode.INVALID_PASSWORD)) {
                return "2";
            }
        }

        //성공
        return "3";
    }

    //마이페이지 수정
    @GetMapping("/{userId}/edit")
    public String editPage(Model model, @PathVariable Long userId) {

        UserDto user = userService.findUserById(userId);

        model.addAttribute("user", user);
        model.addAttribute("myPageEditRequest", new MyPageEditRequest());

        return "users/myPageEdit";
    }

    //마이페이지 정보 수정 완료
    @ResponseBody
    @PostMapping("/{userId}/edit")
    public String editInfo(@PathVariable Long userId, MyPageEditRequest req, Principal principal) {

        //principal 오류 (로그인 오류, 만료)
        try {
            userService.editUserInfo(req.getPassword(), req.getNickname(), principal.getName());
        } catch (Exception e) {
            return "로그인 정보가 유효하지 않습니다. 다시 로그인 해주세요.*login";
        }

        return "변경이 완료되었습니다.*/mypage/"+userId;
    }

}
