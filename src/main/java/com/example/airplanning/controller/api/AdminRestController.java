package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.admin.UserFoundbyAdmin;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.service.AdminService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    @GetMapping("/rankup-accepted/{id}")
    public ResponseEntity<UserDto> rankUpToPlanner(@PathVariable Long id) {
        UserDto userDto = adminService.changeRank(id, "PLANNER");
        return ResponseEntity.ok().body(userDto);
    }

    // 관리자 페이지에서 유저 등급 조절
    @GetMapping("/rankchange/{id}/{role}")
    public ResponseEntity<UserDto> changeRank(@PathVariable Long id, String role) {
        UserDto userDto = adminService.changeRank(id, role);
        return ResponseEntity.ok().body(userDto);
    }


}
