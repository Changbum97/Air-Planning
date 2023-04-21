package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.comment.CommentCreateRequest;
import com.example.airplanning.domain.dto.comment.CommentResponse;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.repository.UserRepository;
import com.example.airplanning.service.CommentService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class CommentRestControllerTest {

    @MockBean
    CommentService commentService;
    @MockBean
    UserService userService;
    @MockBean
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    @DisplayName("댓글 작성 실패 - 비로그인")
    void createComment_fail1 () throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "test", "board");

        // then
        mockMvc.perform(post("/api/comments")
                        .with(csrf())
                        .param("postId", "1")
                        .param("content", "test")
                        .param("commentType", "board"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 작성 실패 - UserDetail 없음")
    void createComment_fail2 () throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        CommentResponse response = CommentResponse.builder().content("testContent").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.createComment(any(), any())).thenReturn(response);

        // then
        mockMvc.perform(post("/api/comments")
                        .with(csrf())
                        .param("postId", "1")
                        .param("content", "test")
                        .param("commentType", "board"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 작성 성공")
    void createComment_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();
        CommentResponse response = CommentResponse.builder().content("testContent").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.createComment(any(), any())).thenReturn(response);

        // then
        mockMvc.perform(post("/api/comments")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("postId", "1")
                        .param("content", "test")
                        .param("commentType", "board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content").value("testContent"));
    }

    @Test
    @WithMockUser
    @DisplayName("대댓글 작성 성공")
    void createCoComment_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();
        CommentResponse response = CommentResponse.builder().content("testCoContent").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.createCoComment(any(), any())).thenReturn(response);

        // then
        mockMvc.perform(post("/api/comments/coco")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("parentId", "1")
                        .param("postId", "1")
                        .param("content", "test")
                        .param("commentType", "board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content").value("testCoContent"));
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 목록 조회 성공")
    void readCommentList_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();

        Pageable pageable = PageRequest.of(0,10);
        List<CommentResponse> commentList = new ArrayList<>();
        commentList.add(CommentResponse.builder().id(1L).content("content1").build());
        commentList.add(CommentResponse.builder().id(2L).content("content2").build());
        commentList.add(CommentResponse.builder().id(3L).content("content3").build());
        Page<CommentResponse> commentPage = new PageImpl<>(commentList, pageable, 1);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.readComment(1L, "board", pageable)).thenReturn(commentPage);

        // then
        mockMvc.perform(get("/api/comments/1/board").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"));
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 성공")
    void updateComment_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();
        CommentResponse response = CommentResponse.builder().content("updatedContent").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.updateComment(any(), any())).thenReturn(response);

        // then
        mockMvc.perform(put("/api/comments")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("targetCommentId", "1")
                        .param("postId", "1")
                        .param("content", "test")
                        .param("commentType", "board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content").value("updatedContent"));
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        UserDetail userDetail = UserDetail.builder().id(1L).userName("user").password("pwd").role("USER").build();

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(commentService.deleteComment(any(), any())).thenReturn(1L);

        // then
        mockMvc.perform(delete("/api/comments")
                        .with(csrf())
                        .with(user(userDetail))
                        .param("targetCommentId", "1")
                        .param("postId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("댓글이 삭제되었습니다."));
    }
}