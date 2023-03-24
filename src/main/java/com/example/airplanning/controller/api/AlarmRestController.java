package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.AlarmResponse;
import com.example.airplanning.service.AlarmService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alarm")
@Slf4j
public class AlarmRestController {
    private  final AlarmService alarmService;

    @ApiIgnore
    @GetMapping(value = "/sub", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetail userDetail, @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        // 로그인 중일 때만 sse 구독 시도.
        if (userDetail == null )  {
            return null;
        } else {
            return alarmService.subscribe(userDetail.getId());
        }
    }

    @ApiIgnore
    @GetMapping("/check/{alarmId}")
    public void alarmCheck(@PathVariable Long alarmId, @AuthenticationPrincipal UserDetail userDetail) {
        alarmService.alarmCheckAndDelete(alarmId, userDetail.getId());
    }

    @GetMapping("/list")
    @ApiOperation(value = "알림 리스트 확인", notes = "현재 존재하는 알림을 5개 단위로 확인합니다.")
    public Response<List<AlarmResponse>> getAlarmList(@ApiIgnore @AuthenticationPrincipal UserDetail userDetail, @ApiIgnore  @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userDetail != null) {
            Page<AlarmResponse> alarms = alarmService.getAlarmList(userDetail.getId(), pageable);
            return Response.success(alarms.getContent());
        }
        return Response.success(null);
    }
}