package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.plan.*;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanRestController {

    private final PlanService planService;

    @PostMapping("")
    public Response<PlanCreateResponse> create(@RequestBody PlanCreateRequest planCreateRequest, Principal principal){
        PlanDto planDto = planService.create(planCreateRequest,"test");

        return Response.success(PlanCreateResponse.of(planDto));
    }

    @GetMapping("/{planId}")
    public Response<PlanDto> detail(@PathVariable Long planId, String userName){
        PlanDto planDto = planService.detail(planId, userName);
        return Response.success(planDto);
    }

    @PutMapping("/{planId}")
    public Response<PlanUpdateResponse> update(@PathVariable Long planId, PlanUpdateRequest updateRequest, Principal principal){
        String userName = principal.getName();
        Long updatePlan = planService.update(planId, updateRequest, userName);

        return Response.success(new PlanUpdateResponse("플랜 수정 완료", updatePlan));
    }

    @DeleteMapping("/{planId}")
    public Response<PlanDeleteResponse> delete(@PathVariable Long planId){
        Long deletePlan = planService.delete(planId, "kakao_2637777345");

        return Response.success(new PlanDeleteResponse(deletePlan));
    }

}
