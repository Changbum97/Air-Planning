package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.dto.review.ReviewListResponse;
import com.example.airplanning.domain.dto.review.ReviewResponse;
import com.example.airplanning.domain.dto.review.ReviewUpdateRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.ReviewService;
import com.example.airplanning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReviewRestControllerTest {

    @MockBean
    ReviewService reviewService;
    @MockBean
    LikeService likeService;
    @MockBean
    UserService userService;
    @MockBean
    Review review;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;

    ReviewCreateRequest reviewCreateRequest;
    ReviewResponse reviewResponse;
    UserDto userDto;

    @BeforeEach
    void beforeEach() {
        reviewCreateRequest = new ReviewCreateRequest(5, 1L, "title", "content");
        reviewResponse = new ReviewResponse(1L, "testPlanner", "testUserNickname", "title", "content", "2023.03.22", "2023.03.22", 5,0);
        userDto = UserDto.builder().userName("user").nickname("testUserNickname").build();
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 작성 성공")
    void createReview_success() throws Exception {
        // given - 업로드 할 파일, 리뷰 작성 요청 내용(ReviewCreateRequest)
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "test file".getBytes(StandardCharsets.UTF_8));
        String reviewCreateRequestJson = objectMapper.writeValueAsString(reviewCreateRequest);
        MockPart mockPart = new MockPart("request", reviewCreateRequestJson.getBytes(StandardCharsets.UTF_8));

        // when - 닉네임 필터 통과, 리뷰 작성
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.write(any(ReviewCreateRequest.class), any(MultipartFile.class), any(String.class))).thenReturn(reviewResponse);

        // then
        mockMvc.perform(multipart("/api/reviews")
                        .file(multipartFile)
                        .part(mockPart)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.title").value("title"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("리뷰 작성 실패 - 비 로그인")
    void createReview_fail() throws Exception {
        // given - 업로드 할 파일, 리뷰 작성 요청 내용(ReviewCreateRequest)
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "text/plain", "test file".getBytes(StandardCharsets.UTF_8));
        String reviewCreateRequestJson = objectMapper.writeValueAsString(reviewCreateRequest);
        MockPart mockPart = new MockPart("request", reviewCreateRequestJson.getBytes(StandardCharsets.UTF_8));

        // when - 리뷰 작성
        when(reviewService.write(any(ReviewCreateRequest.class), any(MultipartFile.class), any(String.class))).thenReturn(reviewResponse);

        // then
        mockMvc.perform(multipart("/api/reviews")
                        .file(multipartFile)
                        .part(mockPart)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 상세 조회 성공 - 조회수 증가")
    void readReview_success_viewTrue() throws Exception {

        // give
        MockCookie cookie = new MockCookie("firstView", "0");

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.findById(1L, true)).thenReturn(review);

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        mockMvc.perform(get("/api/reviews/1").cookie(cookie).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.title").value("title"))
                .andExpect(cookie().value("reviewView", "1"))
                .andExpect(jsonPath("$.result.views").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 상세 조회 성공 - 조회수 증가X")
    void readReview_success_viewFalse() throws Exception {

        // give
        MockCookie cookie = new MockCookie("reviewView", "1");

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.findById(1L, false)).thenReturn(review);

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(1);

        // then
        mockMvc.perform(get("/api/reviews/1").cookie(cookie).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.title").value("title"))
                .andExpect(jsonPath("$.result.views").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 목록 조회 성공")
    void readReviewList_success() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0,10);

        List<ReviewListResponse> reviewList = new ArrayList<>();
        reviewList.add(new ReviewListResponse(1L, "user1", "planner1", "title1", "2023.01.01"));
        reviewList.add(new ReviewListResponse(2L, "user2", "planner2", "title2", "2023.01.01"));
        reviewList.add(new ReviewListResponse(3L, "user3", "planner3", "title3", "2023.01.01"));
        Page<ReviewListResponse> reviewPage = new PageImpl<>(reviewList, pageable, 1);

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.reviewList(any(), any(), any())).thenReturn(reviewPage);

        // then
        mockMvc.perform(get("/api/reviews/list").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content").exists())
                .andExpect(jsonPath("$.result.totalElements").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() throws Exception {
        // given
        ReviewUpdateRequest reviewUpdateRequest = new ReviewUpdateRequest("updatedTitle", "updatedContent", 1, "updatedImage");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image".getBytes(StandardCharsets.UTF_8));
        String reviewUpdateRequestJson = objectMapper.writeValueAsString(reviewUpdateRequest);
        MockPart mockPart = new MockPart("request", reviewUpdateRequestJson.getBytes(StandardCharsets.UTF_8));

        ReviewResponse updatedReviewResponse = new ReviewResponse(1L, "testPlanner", "testUserNickname", "updatedTitle", "updateContent", "2023.01.01", LocalDateTime.now().toString(), 1,0);

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/reviews/1");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });

        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.update(any(Long.class), any(ReviewUpdateRequest.class), any(MultipartFile.class), any(String.class))).thenReturn(updatedReviewResponse);

        // then
        mockMvc.perform(builder
                        .file(multipartFile)
                        .part(mockPart)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.title").value("updatedTitle"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() throws Exception {
        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(reviewService.delete(1L, "user")).thenReturn(1L);

        // then
        mockMvc.perform(delete("/api/reviews/1").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("1번 리뷰가 삭제되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 누르기")
    void like_success() throws Exception {
        // when
        when(userService.findUser("user")).thenReturn(userDto);
        when(likeService.changeLike(1L, "user", LikeType.REVIEW_LIKE)).thenReturn("좋아요가 추가되었습니다.");

        // then
        mockMvc.perform(post("/api/reviews/1/like").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("좋아요가 추가되었습니다."));
    }

}