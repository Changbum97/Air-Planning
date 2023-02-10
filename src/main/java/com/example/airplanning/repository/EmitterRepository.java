package com.example.airplanning.repository;

import com.example.airplanning.domain.dto.AlarmResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@NoArgsConstructor
@Slf4j
@Getter
public class EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, AlarmResponse> eventCache = new ConcurrentHashMap<>();

    public SseEmitter save(Long emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        log.info("실시간 연결 현황 : {}", emitters.entrySet());
        return sseEmitter;
    }
    public SseEmitter findEmitterByUserId(Long userId) {
        return emitters.get(userId);
    }
    public void deleteById(Long id) {
        emitters.remove(id);
    }

}
