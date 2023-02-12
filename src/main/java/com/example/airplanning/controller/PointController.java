package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.point.*;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.enum_class.PointStatus;
import com.example.airplanning.service.PointService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    //form controller

    private final PointService pointService;
    private final UserService userService;

    //포인트 충전 페이지
    @GetMapping("/charging")
    public String payment(Principal principal, PointVo pointVo, Model model, PointRequest pointRequest){
        UserDto user = userService.findUser(principal.getName());

        Integer amount= pointVo.getAmount();
        model.addAttribute("user",user);
        model.addAttribute("amount", amount);

        return "points/payment";
    }

    //포인트 결제 내역 상세
    @GetMapping("/{pointId}")
    public String pointDetail(@PathVariable Long pointId, Principal principal, Model model){
        UserDto user = userService.findUser(principal.getName());
        PointInfoResponse pointResponse = pointService.getOrderDetail(principal.getName(), pointId);

        model.addAttribute("user", user);
        model.addAttribute("point", pointResponse);

        if (pointResponse.getPointStatus().equals(PointStatus.POINT_COMPLETE)){
            return "points/pointDetail";
        }else {
            return "points/pointCancel";
        }
    }

    //포인트 결제 내역 리스트
    @GetMapping("/list")
    public String pointList(Principal principal, Model model, @PageableDefault(size = 20) Pageable pageable){
        UserDto user = userService.findUser(principal.getName());
        Page<PointInfoResponse> infoResponses = pointService.orderList(principal.getName(), pageable);

        model.addAttribute("point", infoResponses);
        model.addAttribute("user", user);

        return "points/list";
    }

}
