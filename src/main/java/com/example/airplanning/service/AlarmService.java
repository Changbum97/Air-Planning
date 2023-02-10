package com.example.airplanning.service;

import com.example.airplanning.domain.dto.AlarmResponse;
import com.example.airplanning.domain.entity.Alarm;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.AlarmRepository;
import com.example.airplanning.repository.EmitterRepository;

import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    public Page<AlarmResponse> getAlarmList (Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<AlarmResponse> alarms = alarmRepository.findAllByUser(user, pageable).map(alarm -> AlarmResponse.of(alarm));

        return alarms;
    }

    @Transactional
    public void alarmCheckAndDelete(Long alarmId, Long userId) {
        System.out.println("여기 요청이 들어오긴 하는거야??");
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new AppException(ErrorCode.AlARM_NOT_FOUND));

        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        System.out.println("알람의 유저아이디 : "+alarm.getUser().getId());
        System.out.println("유저의 유저아이디 : "+user.getId());
        System.out.println("입력받은 유저아이 : "+userId);
        if (alarm.getUser().getId() == user.getId()) {
            System.out.println("요청은 들어왔는데 왜 안지워");
            alarmRepository.delete(alarm);
            System.out.println("요청은 들어왔는데 대체 왜?");
        }
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        try {
                // 연결 시도. 503 에러 발생 방지를 위한 더미 데이터
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

    public void send(User receiver, AlarmType alarmType, String url, String title) {
        System.out.println("알람 보내기 요청이 들어오긴 했습니다.");
        Alarm alarm = Alarm.builder()
                .user(receiver)
                .alarmType(alarmType)
                .targetUrl(url)
                .title(title)
                .build();
        System.out.println("알람저장 시도");
        Alarm savedAlarm = alarmRepository.save(alarm);
        System.out.println("알람 저장 완료");

        SseEmitter emitters = emitterRepository.findEmitterByUserId(receiver.getId());
        if (emitters != null) {
            sendNotification(emitters, receiver.getId(), AlarmResponse.of(savedAlarm));
        }
    }
}




