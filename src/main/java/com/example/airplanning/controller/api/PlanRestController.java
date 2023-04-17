package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.plan.*;
import com.example.airplanning.service.PlanService;
import com.example.airplanning.service.PlannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Api("Plan Controller")
public class PlanRestController {

    private final PlanService planService;
    private final PlannerService plannerService;


    @PostMapping
    @ApiOperation(value = "플랜 신청서 작성", notes = "로그인 한 사용자만 작성이 가능합니다.")
    public Response<PlanResponse> create(@RequestBody PlanCreateRequest request, Principal principal) {
        PlanResponse planResponse = planService.create(request, principal.getName());
        plannerService.findById(request.getPlannerId());
        return Response.success(planResponse);
    }



    @GetMapping("/{planId}")
    @ApiOperation(value = "플랜 신청서 상세 조회", notes = "플랜 신청서의 상세 내용을 확인합니다. 작성자와 해당 플래너만 조회가 가능합니다.")
    @ApiImplicitParam(name = "planId", value = "플랜 신청서 번호", defaultValue = "None")
    public Response<PlanResponse> detail(@PathVariable Long planId, @ApiIgnore Principal principal) {
        PlanResponse planResponse = planService.detail(planId, principal.getName());
        return Response.success(planResponse);
    }

    @PutMapping("/{planId}")
    @ApiOperation(value = "플랜 신청서 수정", notes = "플랜 신청서를 수정합니다. 작성자와 관리자만 수정이 가능합니다.")
    @ApiImplicitParam(name = "planId", value = "플랜 신청서 번호", defaultValue = "None")
    public Response<PlanUpdateResponse> update(@PathVariable Long planId, @RequestBody PlanUpdateRequest updateRequest, @ApiIgnore Principal principal) {
        Long updatePlan = planService.update(planId, updateRequest, principal.getName());

        return Response.success(new PlanUpdateResponse("플랜 수정 완료", updatePlan));
    }

    @DeleteMapping("/{planId}")
    @ApiOperation(value = "플랜 신청서 삭제", notes = "플랜 신청서를 삭제합니다. 작성자와 관리자만 삭제가 가능합니다.")
    @ApiImplicitParam(name = "planId", value = "플랜 신청서 번호", defaultValue = "None")
    public Response<PlanDeleteResponse> delete(@PathVariable Long planId, @ApiIgnore Principal principal) {
        Long deletePlan = planService.delete(planId, principal.getName());

        return Response.success(new PlanDeleteResponse(deletePlan));
    }

    @GetMapping("/list")
    @ApiOperation(value = "플랜 신청서 목록 조회", notes = "플랜 신청서 목록을 확인합니다.")
    public Response<Page<PlanListResponse>> planList(@PageableDefault(sort = "createdAt", size = 20, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PlanListResponse> list = planService.list(pageable);
        return Response.success(list);
    }

    @GetMapping("/{planId}/refuse")
    @ApiOperation(value = "플랜 신청 거절", notes = "플래너가 자신에게 들어온 플랜을 거절합니다.")
    public Response<PlanResponse> refusePlan(@PathVariable Long planId, @ApiIgnore Principal principal) {
        PlanResponse refuse = planService.refusePlan(planId, principal.getName());
        return Response.success(refuse);
    }

    @GetMapping("/{planId}/accept")
    @ApiOperation(value = "플랜 신청 수락", notes = "플래너가 자신에게 들어온 플랜을 수락합니다.")
    public Response<PlanResponse> acceptPlan(@PathVariable Long planId, @ApiIgnore Principal principal){
        PlanResponse accept = planService.acceptPlan(planId, principal.getName());
        return Response.success(accept);
    }

    @PostMapping("/{planId}/payment")
    public Response<String> pointPayment(@PathVariable Long planId, Principal principal){
        planService.usedPoint(principal.getName(), planId);

        return Response.success("결제가 완료되었습니다.");
    }

}
