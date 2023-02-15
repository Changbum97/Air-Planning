package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.dto.planner.PlannerPlanResponse;
import com.example.airplanning.service.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    @GetMapping("/{plannerId}")
    public String detail(@PathVariable Long plannerId, Model model, Principal principal) {
        PlannerDetailResponse response = plannerService.findById(plannerId);
        model.addAttribute("planner", response);
        model.addAttribute("plannerName", principal.getName());
        return "users/planner";
    }

    @ResponseBody
    @GetMapping("/{plannerId}/trip-waiting")
    public Page<PlannerPlanResponse> getWaitingPlan(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                                                    @PathVariable Long plannerId) {
        Page<PlannerPlanResponse> planPage = plannerService.getWaitingPlan(plannerId, pageable);
        return planPage;
    }

    @ResponseBody
    @GetMapping("/{plannerId}/trip-accepted")
    public Page<PlannerPlanResponse> getAcceptedPlan(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                                                    @PathVariable Long plannerId) {
        Page<PlannerPlanResponse> planPage = plannerService.getAcceptedPlan(plannerId, pageable);
        return planPage;
    }

    @ResponseBody
    @GetMapping("/{plannerId}/trip-refused")
    public Page<PlannerPlanResponse> getRefusedPlan(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                                                     @PathVariable Long plannerId) {
        Page<PlannerPlanResponse> planPage = plannerService.getRefusedPlan(plannerId, pageable);
        return planPage;
    }

    @ResponseBody
    @GetMapping("/{plannerId}/trip-completed")
    public Page<PlannerPlanResponse> getCompletedPlan(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
                                                    @PathVariable Long plannerId) {
        Page<PlannerPlanResponse> planPage = plannerService.getCompletedPlan(plannerId, pageable);
        return planPage;
    }

}
