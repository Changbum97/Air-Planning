package com.example.airplanning.controller;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.plan.*;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.service.PlanService;
import com.example.airplanning.service.PlannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final PlannerService plannerService;

    @GetMapping("/write/{plannerId}")
    public String writePlanPage(Model model, @AuthenticationPrincipal UserDetail userDetail, @PathVariable Long plannerId){

        if (userDetail.getUsername().equals(plannerService.findById(plannerId).getUserName())) {
            model.addAttribute("nextPage", "/planners/"+plannerId);
            model.addAttribute("msg", "본인에게는 플랜 신청 할 수 없습니다.");
            return "error/redirect";
        }
        model.addAttribute(new PlanCreateRequest(plannerId));
        return "plans/write";
    }

    @GetMapping("/{planId}")
    public String detailPlan(@PathVariable Long planId, Principal principal, Model model){
        PlanResponse planDto = planService.detail(planId, principal.getName());
        model.addAttribute("plan", planDto);
        model.addAttribute("userName", principal.getName());
        return "plans/detail";
    }

    @GetMapping("/{planId}/update")
    public String updatePlanPage(@PathVariable Long planId, Model model){
        Plan plan = planService.planview(planId);
        model.addAttribute(new PlanUpdateRequest(plan.getTitle(), plan.getContent()));
        return "plans/update";
    }

    @GetMapping("/list")
    public String listPlan(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable, Model model){
        Page<PlanListResponse> planList = planService.list(pageable);
        model.addAttribute("plan", planList);
        return "plans/list";
    }

    @GetMapping("/{planId}/detail")
    public String pointDetail(@PathVariable Long planId, Principal principal, Model model) {
        PlanPaymentRequest paymentRequest = planService.getInfo(principal.getName(), planId);

        model.addAttribute("paymentRequest", paymentRequest);
        model.addAttribute("userName", principal.getName());
        return "plans/payment";
    }


}
