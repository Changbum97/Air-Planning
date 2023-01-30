package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.admin.UserFoundbyAdmin;
import com.example.airplanning.domain.dto.user.UserDto;
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

    @GetMapping("/user-search/{nickname}")
    public ResponseEntity<UserFoundbyAdmin> userFoundbyAdmin (@PathVariable String nickname) {
        UserDto userDto = userService.findNickname(nickname);
        return ResponseEntity.ok().body(UserFoundbyAdmin.of(userDto));
    }


}
