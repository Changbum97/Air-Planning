package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;
    private final LikeService likeService;

    @GetMapping("/{plannerId}")
    public String detail(@PathVariable Long plannerId, Model model, Principal principal) {
        PlannerDetailResponse response = plannerService.findById(plannerId);
        model.addAttribute("planner", response);

        if (principal != null) {
            model.addAttribute("checkLike", likeService.checkPlannerLike(plannerId, principal.getName()));
        } else {
            model.addAttribute("checkLike", false);
        }
        return "users/planner";
    }

    @PostMapping("/{plannerId}/like")
    @ResponseBody
    public String changeLike(@PathVariable Long plannerId, Principal principal) {
        return likeService.changeLike(plannerId, principal.getName(), LikeType.PLANNER_LIKE);
    }
}
