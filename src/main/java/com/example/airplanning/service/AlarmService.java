package com.example.airplanning.service;

import com.example.airplanning.domain.dto.AlarmResponse;
import com.example.airplanning.domain.entity.Alarm;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.repository.AlarmRepository;
import com.example.airplanning.repository.EmitterRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;
    private final AlarmRepository alarmRepository;

    public SseEmitter subscribe(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        try {
                // 연결 시도.
                sseEmitter.send(SseEmitter.event().name("connect"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        // 연결 정보 저장.
        emitterRepository.save(userId, sseEmitter);
        sseEmitter.onCompletion(() -> emitterRepository.deleteById(userId));
        sseEmitter.onTimeout(() -> emitterRepository.deleteById(userId));
        sseEmitter.onError((e)->emitterRepository.deleteById(userId));

        return sseEmitter;
    }

    private void sendNotification(SseEmitter emitter, Long emitterId, AlarmResponse alarmResponse) {
        try {
            emitter.send(SseEmitter.event().name("Alarm")
                            .data(alarmResponse, MediaType.APPLICATION_JSON));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    public void send(User receiver, AlarmType alarmType, String url) {
        System.out.println("알람 보내기 요청이 들어오긴 했습니다.");
        Alarm alarm = Alarm.builder()
                .user(receiver)
                .alarmType(alarmType)
                .targetUrl(url)
                .isChecked(false)
                .build();
        System.out.println("알람저장 시도");
        Alarm savedAlarm = alarmRepository.save(alarm);
        System.out.println("알람 저장 완료");

        SseEmitter emitters = emitterRepository.findEmitterByUserId(receiver.getId());
        if (emitters != null) {
            sendNotification(emitters, receiver.getId(), AlarmResponse.of(savedAlarm));
        }

//        SseEmitter emitters = emitterRepository.findEmitterByUserId(receiver.getId());
//        sendNotification(emitters, receiver.getId(),AlarmResponse.of(savedAlarm) );
    }
}




