package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.plan.PlanCreateRequest;
import com.example.airplanning.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping("/write")
    public String writePlanPage(Model model){
        model.addAttribute(new PlanCreateRequest());
        return "plans/write";
    }

    @ResponseBody
    @PostMapping("")
    public String writePlan(PlanCreateRequest createRequest, Principal principal){
        planService.create(createRequest, principal.getName());
        return "플랜 신청서 작성 성공";
    }
}
