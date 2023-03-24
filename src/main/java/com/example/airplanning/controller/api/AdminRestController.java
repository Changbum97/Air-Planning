package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.admin.UserFoundbyAdmin;
import com.example.airplanning.domain.dto.board.RankUpRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.AdminService;
import com.example.airplanning.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import springfox.documentation.annotations.ApiIgnore;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminRestController {
    private final UserService userService;
    private final AdminService adminService;

    // 관리자 페이지에서 닉네임으로 유저 찾기
    @GetMapping("/{nickname}/search")
    @ApiOperation(value = "닉네임으로 유저 찾기", notes = "관리자는 닉네임으로 유저의 정보를 찾을 수 있습니다.")
    @ApiImplicitParam(name = "nickname", value = "유저 닉네임")
    public Response<UserFoundbyAdmin> userFoundbyAdmin (@PathVariable String nickname) {
        UserDto userDto = userService.findNickname(nickname);
        return Response.success(UserFoundbyAdmin.of(userDto));
    }

    // 플래너 등급 신청 수락 버튼
    @PostMapping("/rankup-accepted")
    @ApiOperation(value = "플래너 등급 신청 수락", notes = "관리자는 플래너 등급 신청을 수락할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "유저 아이디"),
            @ApiImplicitParam(name = "description", value = "신청 내용"),
            @ApiImplicitParam(name = "region", value = "지역"),
            @ApiImplicitParam(name = "boardId", value = "신청 글 번호"),
            @ApiImplicitParam(name = "amount", value = "플랜 가격")})
    public Response<UserFoundbyAdmin> rankUpToPlanner(@ModelAttribute RankUpRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        if (userDetail.getRole().equals("ADMIN")) {
            UserDto userDto = adminService.changeRankToPlanner(request);
            return Response.success(UserFoundbyAdmin.of(userDto));
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    // 관리자 페이지에서 유저 등급 조절
    @PostMapping("/rank-change")
    @ApiOperation(value = "유저 등급 조절", notes = "관리자는 신고 게시글을 확인하고 유저의 등급을 조절할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nickname", value = "유저 닉네임"),
            @ApiImplicitParam(name = "boardId", value = "신고 글 번호"),
            @ApiImplicitParam(name = "role", value = "변경할 역할")})
    public Response<UserFoundbyAdmin> changeRank(@RequestParam("nickname") String nickname, @RequestParam("boardId") Long boardId, @RequestParam("role") String role, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        if (userDetail.getRole().equals("ADMIN")) {
            UserDto userDto = adminService.changeRank(nickname, role, boardId);
            return Response.success(UserFoundbyAdmin.of(userDto));
        } else {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

    }


}
