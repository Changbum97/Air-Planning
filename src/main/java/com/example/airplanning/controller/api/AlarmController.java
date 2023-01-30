package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequiredArgsConstructor
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
}