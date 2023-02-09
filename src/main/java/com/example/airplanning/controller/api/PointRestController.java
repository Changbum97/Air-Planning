package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.point.PointRequest;
import com.example.airplanning.domain.dto.point.PointResponse;
import com.example.airplanning.domain.dto.point.PointVo;
import com.example.airplanning.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
@Slf4j
public class PointRestController {

    private final PointService pointService;

    @PostMapping("/charge")
    public Response<PointResponse> charge(@RequestBody PointVo pointVo, Principal principal){
        String userName = "test";
        PointResponse pointResponse = pointService.chargePoint(userName, pointVo);
        return Response.success(pointResponse);
    }


}
