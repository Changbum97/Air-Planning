package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.service.MyPageService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyPageRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MyPageRestControllerTest {

    @MockBean
    MyPageService myPageService;
    @MockBean
    UserService userService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("유저 정보 조회 성공")
    void getMyInfo_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        MyPageInfoResponse infoResponse = new MyPageInfoResponse("nickname", "user");

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getMyPageInfo("user")).thenReturn(infoResponse);

        // then
        mockMvc.perform(get("/api/users/1/mypage/info")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("nickname"));
    }

    @Test
    @WithMockUser
    @DisplayName("유저 게시글 조회 성공")
    void getMyBoard_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPageBoardResponse> list = new ArrayList<>();
        list.add(new MyPageBoardResponse(1L,"title","category","9999.99.99"));
        list.add(new MyPageBoardResponse(2L,"title","category","9999.99.99"));
        list.add(new MyPageBoardResponse(3L,"title","category","9999.99.99"));
        Page<MyPageBoardResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getMyBoard(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/my/boards")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("유저 리뷰 조회 성공")
    void getMyReview_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPageReviewResponse> list = new ArrayList<>();
        list.add(new MyPageReviewResponse(1L,"title","9999.99.99","planner"));
        list.add(new MyPageReviewResponse(2L,"title","9999.99.99","planner"));
        list.add(new MyPageReviewResponse(3L,"title","9999.99.99","planner"));
        Page<MyPageReviewResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getMyReview(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/my/reviews")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("유저 댓글 조회 성공")
    void getMyComment_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPageCommentResponse> list = new ArrayList<>();
        list.add(new MyPageCommentResponse(1L,"content",2L,"parentType","parentTitle","9999.99.99"));
        list.add(new MyPageCommentResponse(2L,"content",2L,"parentType","parentTitle","9999.99.99"));
        list.add(new MyPageCommentResponse(3L,"content",2L,"parentType","parentTitle","9999.99.99"));
        Page<MyPageCommentResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getMyComment(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/my/comments")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 누른 게시글 조회 성공")
    void getLikeBoard_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPageBoardResponse> list = new ArrayList<>();
        list.add(new MyPageBoardResponse(1L,"title","category","9999.99.99"));
        list.add(new MyPageBoardResponse(2L,"title","category","9999.99.99"));
        list.add(new MyPageBoardResponse(3L,"title","category","9999.99.99"));
        Page<MyPageBoardResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getLikeBoard(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/like/boards")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 누른 리뷰 조회 성공")
    void getLikeReview_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPageReviewResponse> list = new ArrayList<>();
        list.add(new MyPageReviewResponse(1L,"title","9999.99.99","planner"));
        list.add(new MyPageReviewResponse(2L,"title","9999.99.99","planner"));
        list.add(new MyPageReviewResponse(3L,"title","9999.99.99","planner"));
        Page<MyPageReviewResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getLikeReview(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/like/reviews")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 누른 플래너 조회 성공")
    void getLikePlanner_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPagePlannerResponse> list = new ArrayList<>();
        list.add(new MyPagePlannerResponse(1L,"name","5","9999.99.99"));
        list.add(new MyPagePlannerResponse(2L,"name","5","9999.99.99"));
        list.add(new MyPagePlannerResponse(3L,"name","5","9999.99.99"));
        Page<MyPagePlannerResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getLikePlanner(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/like/planners")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("여행중 상태의 플랜 조회 성공")
    void getProgressPlan_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPagePlanResponse> list = new ArrayList<>();
        list.add(new MyPagePlanResponse(1L,"title",2L, PlanType.ACCEPT,false,"9999.99.99"));
        list.add(new MyPagePlanResponse(2L,"title",2L, PlanType.ACCEPT,false,"9999.99.99"));
        list.add(new MyPagePlanResponse(3L,"title",2L, PlanType.ACCEPT,false,"9999.99.99"));
        Page<MyPagePlanResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getProgressPlan(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/trip/progress")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("여행완료 상태의 플랜 조회 성공")
    void getFinishPlan_success() throws Exception {
        // given
        UserDto userDto = UserDto.builder().id(1L).nickname("user").build();
        List<MyPagePlanResponse> list = new ArrayList<>();
        list.add(new MyPagePlanResponse(1L,"title",2L, PlanType.ACCEPT,true,"9999.99.99"));
        list.add(new MyPagePlanResponse(2L,"title",2L, PlanType.ACCEPT,true,"9999.99.99"));
        list.add(new MyPagePlanResponse(3L,"title",2L, PlanType.ACCEPT,true,"9999.99.99"));
        Page<MyPagePlanResponse> page = new PageImpl<>(list);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(myPageService.getFinishPlan(any(), any())).thenReturn(page);

        // then
        mockMvc.perform(get("/api/users/1/mypage/trip/finish")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(3));
    }

}