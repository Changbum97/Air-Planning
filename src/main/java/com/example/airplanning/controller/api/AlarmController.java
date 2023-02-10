package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.AlarmResponse;
import com.example.airplanning.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
@Slf4j
public class AlarmController {
    private  final AlarmService alarmService;

    @GetMapping(value = "/sub", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetail userDetail, @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        // 로그인 중일 때만 sse 구독 시도.
        if (userDetail == null )  {
            return null;
        } else {
            return alarmService.subscribe(userDetail.getId());
        }
    }
    @GetMapping("/check/{alarmId}")
    public void alarmCheck(@PathVariable Long alarmId, @AuthenticationPrincipal UserDetail userDetail) {
        alarmService.alarmCheckAndDelete(alarmId, userDetail.getId());
    }

    @GetMapping("/getList")
    public ResponseEntity getAlarmList(@AuthenticationPrincipal UserDetail userDetail, @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userDetail != null) {
            Page<AlarmResponse> alarms = alarmService.getAlarmList(userDetail.getId(), pageable);
            return ResponseEntity.ok().body(alarms.getContent());
        }
        return ResponseEntity.ok().body(null);
    }
}