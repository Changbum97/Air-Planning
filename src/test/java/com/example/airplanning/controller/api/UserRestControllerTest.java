package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.user.FindByEmailRequest;
import com.example.airplanning.domain.dto.user.SetNicknameRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.dto.user.UserJoinRequest;
import com.example.airplanning.service.EmailService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class UserRestControllerTest {

    @MockBean
    UserService userService;
    @MockBean
    EmailService emailService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("회원가입 - 성공")
    void join_success() throws Exception {
        // given
        UserJoinRequest userJoinRequest = new UserJoinRequest("testNick", "testEmail", "testName", "password", "code");
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();
        UserDto userDto = spy(UserDto.builder().nickname("testNick").build());

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(userService.join(any())).thenReturn(userDto);

        // then
        mockMvc.perform(post("/api/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.nickname").value("testNick"));
    }

    @Test
    @WithMockUser
    @DisplayName("아이디 중복 체크")
    void checkUsername() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(userService.checkUserName("testName")).thenReturn(true);

        // then
        mockMvc.perform(get("/api/users/check-username")
                                    .with(csrf())
                                    .param("username", "testName"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("닉네임 중복 체크")
    void checkNickname() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(userService.checkNickname("testNick")).thenReturn(true);

        // then
        mockMvc.perform(get("/api/users/check-nickname")
                        .with(csrf())
                        .param("nickname", "testNick"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("이메일 중복 체크")
    void checkEmail() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(userService.checkEmail("email")).thenReturn(true);

        // then
        mockMvc.perform(get("/api/users/check-email")
                        .with(csrf())
                        .param("email", "email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("인증 메일 보내기")
    void sendAuthEmail() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(emailService.sendLoginAuthMessage("email")).thenReturn("인증 메일이 발송되었습니다.");

        // then
        mockMvc.perform(get("/api/users/send-auth-email")
                        .with(csrf())
                        .param("email", "email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("인증 메일이 발송되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("인증 메일 확인 - 불일치")
    void checkAuthEmail_false() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(emailService.getData("code")).thenReturn(null);

        // then
        mockMvc.perform(get("/api/users/check-auth-email")
                        .with(csrf())
                        .param("code", "code"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("인증 메일 확인 - 일치")
    void checkAuthEmail_true() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(emailService.getData("code")).thenReturn("key");

        // then
        mockMvc.perform(get("/api/users/check-auth-email")
                        .with(csrf())
                        .param("code", "code"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("이메일로 아이디 찾기")
    void findIdByEmail() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();
        FindByEmailRequest findByEmailRequest = new FindByEmailRequest("testName", "email");

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(emailService.sendFoundIdMessage(any())).thenReturn("메일로 아이디를 전송했습니다.");

        // then
        mockMvc.perform(get("/api/users/find-id-by-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findByEmailRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("메일로 아이디를 전송했습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("이메일로 비밀번호 찾기")
    void findPwByEmail() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();
        FindByEmailRequest findByEmailRequest = new FindByEmailRequest("testName", "email");

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);
        when(emailService.sendFoundPasswordMessage(any(), any())).thenReturn("메일로 새로운 비밀번호를 전송했습니다.");

        // then
        mockMvc.perform(get("/api/users/find-pw-by-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findByEmailRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("메일로 새로운 비밀번호를 전송했습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("닉네임 설정")
    void setNickname() throws Exception {
        // given
        UserDto nickNameFilter = UserDto.builder().id(1L).nickname("user").build();
        SetNicknameRequest setNicknameRequest = new SetNicknameRequest("testName");
        setNicknameRequest.setNickname("testNick");

        // when
        when(userService.findUser("user")).thenReturn(nickNameFilter);

        // then
        mockMvc.perform(post("/api/users/set-nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setNicknameRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"));
    }
}