package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.plan.PlanCreateRequest;
import com.example.airplanning.domain.dto.plan.PlanDto;
import com.example.airplanning.domain.dto.plan.PlanUpdateRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/plans")
@RequiredArgsConstructor
@Slf4j
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
        return "redirect:/plans/{planId}";
    }

    @GetMapping("/{planId}")
    public String detailPlan(@PathVariable Long planId, Principal principal, Model model){
        PlanDto planDto = planService.detail(planId, principal.getName());
        model.addAttribute("plan", planDto);
        model.addAttribute("userName", principal.getName());
        log.info(planDto.getUserRole().name());
        return "plans/detail";
    }

    @GetMapping("/{planId}/update")
    public String updatePlanPage(@PathVariable Long planId, Model model){
        Plan plan = planService.planview(planId);
        model.addAttribute(new PlanUpdateRequest(plan.getTitle(), plan.getContent()));
        return "plans/update";
    }

    @PostMapping("/{planId}/update")
    public String updatePlan(@PathVariable Long planId, PlanUpdateRequest updateRequest, Principal principal, Model model){
        planService.update(planId, updateRequest, principal.getName());
        model.addAttribute("planId", planId);
        /*String url = "redirect:detail?&planId="+planId;*/
        return "redirect:/plans/{planId}";
    }

    @ResponseBody
    @GetMapping("/{planId}/delete")
    public String deletePlan(@PathVariable Long planId, Principal principal){
        planService.delete(planId, principal.getName());
        return "redirect:/plans/list";
    }

    @GetMapping("/list")
    public String listPlan(Pageable pageable, Model model){
        Page<PlanDto> planDtos = planService.list(pageable);
        model.addAttribute("plan", planDtos);
        return "plans/list";
    }

    @ResponseBody
    @GetMapping("/{planId}/refuse")
    public String refusePlan(@PathVariable Long planId, Principal principal){
        planService.refusePlan(planId, principal.getName());
        return "redirect:/plans/list";
    }

    @ResponseBody
    @GetMapping("/{planId}/accept")
    public String acceptPlan(@PathVariable Long planId, Principal principal){
        planService.acceptPlan(planId, principal.getName());
        return "redirect:/plans/list";
    }

}
