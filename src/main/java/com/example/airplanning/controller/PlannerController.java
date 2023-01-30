package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.service.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    @GetMapping("/{plannerId}")
    public String detail(@PathVariable Long plannerId, Model model) {
        PlannerDetailResponse response = plannerService.findById(plannerId);
        model.addAttribute("planner", response);
        return "/users/planner";
    }
}
