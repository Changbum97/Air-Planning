package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.point.PointRequest;
import com.example.airplanning.domain.dto.point.PointResponse;
import com.example.airplanning.domain.dto.point.PointVo;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.service.PointService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    //form controller

    private final PointService pointService;
    private final UserService userService;

    @GetMapping("/charging")
    public String payment(Principal principal, PointVo pointVo, Model model, PointRequest pointRequest){
        UserDto user = userService.findUser(principal.getName());

        Integer amount= pointVo.getAmount();
        model.addAttribute("user",user);
        model.addAttribute("amount", amount);

        return "/payments/payment";
    }

}
