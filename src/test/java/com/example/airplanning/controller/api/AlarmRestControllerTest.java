package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.AlarmResponse;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.service.AlarmService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@WebMvcTest(AlarmRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class AlarmRestControllerTest {

    @MockBean
    AlarmService alarmService;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    @WithMockUser
    @DisplayName("구독 실패 - UserDetail 없음")
    void subscribe_fail() throws Exception {

        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);

        // then
        MvcResult result = mockMvc.perform(get("/api/alarm/sub")
                                                    .with(csrf()))
                                    .andDo(print())
                                    .andExpect(status().isOk())
                                    .andReturn();

        String content = result.getResponse().getContentAsString();

        assertThat("", is(content));
    }

    @Test
    @WithMockUser
    @DisplayName("구독 성공")
    void subscribe_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").role("USER").build();
        SseEmitter sseEmitter = new SseEmitter(3600L);


        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(alarmService.subscribe(1L)).thenReturn(sseEmitter);

        // then
        mockMvc.perform(get("/api/alarm/sub")
                        .with(csrf())
                        .with(user(userDetail)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("알람 확인, 삭제")
    void alarmCheck_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").role("USER").build();


        // when
        when(userService.findUser("user")).thenReturn(userDto);

        // then
        mockMvc.perform(get("/api/alarm/check/1")
                        .with(csrf())
                        .with(user(userDetail)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("알람 리스트 호출 실패 - UserDetail 없음")
    void getAlarmList_fail() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);

        // then
        mockMvc.perform(get("/api/alarm/list")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("알람 리스트 호출 성공")
    void getAlarmList_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").role("USER").build();
        Pageable pageable = PageRequest.of(0,5);

        List<AlarmResponse> alarmList = new ArrayList<>();
        alarmList.add(new AlarmResponse(1L, "url", "test", "title", "test" ));
        alarmList.add(new AlarmResponse(2L, "url2", "test2", "title2", "test2" ));
        Page<AlarmResponse> alarmPage = new PageImpl<>(alarmList);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(alarmService.getAlarmList(any(), any() )).thenReturn(alarmPage);

        // then
        mockMvc.perform(get("/api/alarm/list")
                        .with(csrf())
                        .with(user(userDetail)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }




}