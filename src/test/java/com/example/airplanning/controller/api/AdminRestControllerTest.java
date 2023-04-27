package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.board.RankUpRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.service.AdminService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class AdminRestControllerTest {

    @MockBean
    UserService userService;

    @MockBean
    AdminService adminService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("닉네임으로 유저 찾기 성공")
    void findUserByNick_success() throws Exception {
        // then
        UserDto admin = UserDto.builder().nickname("admin").role("ADMIN").build();
        UserDto userDto = UserDto.builder().nickname("userNick").role("USER").build();

        // when
        when(userService.findUser("admin")).thenReturn(admin);
        when(userService.findNickname("userNick")).thenReturn(userDto);

        // then
        mockMvc.perform(get("/api/admin/userNick/search").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.nickname").value("userNick"));
    }

    @Test
    @WithMockUser
    @DisplayName("플래너 등업 요청 수락 실패 - 관리자 아님")
    void rankUpRequestAccept_fail() throws Exception {
        // then
        RankUpRequest request = new RankUpRequest("user", "description", "region", 1L, 1000 );
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();
        UserDto userDto = UserDto.builder().nickname("user").role("USER").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);

        // then
        mockMvc.perform(post("/api/admin/rankup-accepted")
                        .with(csrf())
                        .with(user(userDetail))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("플래너 등업 요청 수락 성공")
    void rankUpRequestAccept_success() throws Exception {
        // then
        RankUpRequest request = new RankUpRequest("user", "description", "region", 1L, 1000 );
        UserDetail userDetail = UserDetail.builder().id(1L).userName("admin").password("pwd").role("ADMIN").build();
        UserDto admin = UserDto.builder().nickname("admin").role("ADMIN").build();
        UserDto targetUser = UserDto.builder().userName("user").nickname("userNick").role("PLANNER").build();

        // when
        when(userService.findUser("admin")).thenReturn(admin);
        when(adminService.changeRankToPlanner(any())).thenReturn(targetUser);

        // then
        mockMvc.perform(post("/api/admin/rankup-accepted")
                        .with(csrf())
                        .with(user(userDetail))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.role").value("PLANNER"));
    }

    @Test
    @WithMockUser
    @DisplayName("유저 등급 조절 실패 - 관리자 아님")
    void rankChange_fail() throws Exception {
        // then
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();
        UserDto userDto = UserDto.builder().nickname("user").role("USER").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);

        // then
        mockMvc.perform(post("/api/admin/rank-change")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("nickname", "user")
                        .param("boardId", "1")
                        .param("role", "BLACKLIST"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("유저 등급 조절 성공")
    void rankChange_success() throws Exception {
        // then
        UserDetail userDetail = UserDetail.builder().id(1L).userName("admin").password("pwd").role("ADMIN").build();
        UserDto userDto = UserDto.builder().nickname("admin").role("USER").build();
        UserDto targetUser = UserDto.builder().userName("user").role("BLACKLIST").build();

        // when
        when(userService.findUser("admin")).thenReturn(userDto);
        when(adminService.changeRank("user", "BLACKLIST", 1L)).thenReturn(targetUser);

        // then
        mockMvc.perform(post("/api/admin/rank-change")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("nickname", "user")
                        .param("boardId", "1")
                        .param("role", "BLACKLIST"))
                .andDo(print())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.role").value("BLACKLIST"));
    }
}