package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.admin.UserFoundbyAdmin;
import com.example.airplanning.domain.dto.board.RankUpRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.AdminService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminRestController {
    private final UserService userService;
    private final AdminService adminService;

    // 관리자 페이지에서 닉네임으로 유저 찾기
    @GetMapping("/user-search/{nickname}")
    public ResponseEntity<UserFoundbyAdmin> userFoundbyAdmin (@PathVariable String nickname) {
        UserDto userDto = userService.findNickname(nickname);
        return ResponseEntity.ok().body(UserFoundbyAdmin.of(userDto));
    }

    // 플래너 등급 신청 수락 버튼
    @ResponseBody
    @PostMapping("/rankup-accepted")
    public ResponseEntity<UserDto> rankUpToPlanner(@ModelAttribute RankUpRequest request, @AuthenticationPrincipal UserDetail userDetail) {
        if (userDetail.getRole().equals("ADMIN")) {
            UserDto userDto = adminService.changeRankToPlanner(request);
            return ResponseEntity.ok().body(userDto);
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    // 관리자 페이지에서 유저 등급 조절
    @PostMapping("/rankchange")
    public ResponseEntity<UserDto> changeRank(@RequestParam("nickname") String nickname, @RequestParam("boardId") Long boardId, @RequestParam("role") String role, @AuthenticationPrincipal UserDetail userDetail) {
        if (userDetail.getRole().equals("ADMIN")) {
            UserDto userDto = adminService.changeRank(nickname, role, boardId);
            return ResponseEntity.ok().body(userDto);
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

    }


}
