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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlarmServiceTest {

    private final EmitterRepository emitterRepository = mock(EmitterRepository.class);
    private final AlarmRepository alarmRepository = mock(AlarmRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    AlarmService alarmService;

    @BeforeEach
    void beforeEach() {
        alarmService = new AlarmService(emitterRepository, alarmRepository, userRepository);
    }

    @Test
    @DisplayName("알람 리스트 호출 실패 - 유저 정보 없음")
    void getAlarmList_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> alarmService.getAlarmList(1L, pageable));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("알람 리스트 호출 성공")
    void getAlarmList_success() {
        // given
        Pageable pageable = PageRequest.of(0,10);
        User user = spy(User.builder().build());
        List<Alarm> alarmList = new ArrayList<>();
        alarmList.add(Alarm.builder().id(1L).targetUrl("url").alarmType(AlarmType.COMMENT_ALARM).title("test").build());
        Page<Alarm> alarmPage = new PageImpl<>(alarmList);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(alarmRepository.findAllByUser(user, pageable)).thenReturn(alarmPage);

        // then
        Page<AlarmResponse> result = alarmService.getAlarmList(1L, pageable);
        assertThat(result.getContent().get(0).getTitle(), is("[test]"));
    }

    @Test
    @DisplayName("알람 확인 실패 - 알람 없음")
    void alarmCheckAndDelete_fail1() {
        // when
        when(alarmRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> alarmService.alarmCheckAndDelete(1L, 1L));
        assertThat(error.getErrorCode(), is(ErrorCode.AlARM_NOT_FOUND));
    }

    @Test
    @DisplayName("알람 확인 실패 - 유저 정보 없음")
    void alarmCheckAndDelete_fail2() {
        // given
        Alarm alarm = spy(Alarm.class);

        // when
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(alarm));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> alarmService.alarmCheckAndDelete(1L, 1L));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("알람 확인 실패 - 유저 정보 다름")
    void alarmCheckAndDelete_fail3() {
        // given
        User user = spy(User.builder().id(2L).build());
        Alarm alarm = spy(Alarm.builder().user(user).build());

        // when
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(alarm));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // then
        alarmService.alarmCheckAndDelete(1L, 1L);
    }

    @Test
    @DisplayName("알람 확인 성공")
    void alarmCheckAndDelete_success() {
        // given
        User user = spy(User.builder().id(1L).build());
        Alarm alarm = spy(Alarm.builder().user(user).build());

        // when
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(alarm));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // then
        alarmService.alarmCheckAndDelete(1L, 1L);
    }

    @Test
    @DisplayName("Sse 구독 성공")
    void subscribe_success() {
        SseEmitter emitter = alarmService.subscribe(1L);
        assertThat(60L*1000*60, is(emitter.getTimeout()));
    }

    @Test
    @DisplayName("알람보내기 실패 - emitter 없음")
    void send_fail() {
        // given
        User receiver = spy(User.builder().id(1L).build());

        // when
        when(emitterRepository.findEmitterByUserId(1L)).thenReturn(null);

        // then
        alarmService.send(receiver, AlarmType.COMMENT_ALARM, "testUrl", "testTitle");
    }

    @Test
    @DisplayName("알람보내기 성공")
    void send_success() {
        // given
        User receiver = spy(User.builder().id(1L).build());
        SseEmitter emitter = spy(SseEmitter.class);
        Alarm savedAlarm = spy(Alarm.builder().id(1L).targetUrl("testUrl").alarmType(AlarmType.COMMENT_ALARM).title("testTitle").build());

        // when
        when(emitterRepository.findEmitterByUserId(1L)).thenReturn(emitter);
        when(alarmRepository.save(any())).thenReturn(savedAlarm);

        // then
        alarmService.send(receiver, AlarmType.COMMENT_ALARM, "testUrl", "testTitle");

    }

}