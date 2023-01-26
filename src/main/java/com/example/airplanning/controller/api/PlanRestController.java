package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.plan.PlanCreateRequest;
import com.example.airplanning.domain.dto.plan.PlanCreateResponse;
import com.example.airplanning.domain.dto.plan.PlanDto;
import com.example.airplanning.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
