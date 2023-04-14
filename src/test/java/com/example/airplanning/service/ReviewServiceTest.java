package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.dto.review.ReviewListResponse;
import com.example.airplanning.domain.dto.review.ReviewResponse;
import com.example.airplanning.domain.dto.review.ReviewUpdateRequest;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.PlannerRepository;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ReviewServiceTest {
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PlannerRepository plannerRepository = mock(PlannerRepository.class);
    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final AmazonS3 amazonS3 = mock(AmazonS3.class);
    private final AlarmService alarmService = mock(AlarmService.class);
    private final Review review = mock(Review.class);
    ReviewService reviewService;

    @BeforeEach
    void beforeEach() {
        reviewService = new ReviewService(reviewRepository, userRepository, plannerRepository, planRepository, amazonS3, alarmService);

    }

    @Test
    @DisplayName("리뷰 작성 실패 - 작성자 없음")
    void reviewWrite_fail1() throws IOException {
        // then
        ReviewCreateRequest request = new ReviewCreateRequest(5, 1L, "title", "content");

        // when
        when(userRepository.findByUserName("AnonymousUser")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.write(request, null, "AnonymousUser"));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 플랜 없음")
    void reviewWrite_fail2() throws IOException {
        // then
        ReviewCreateRequest request = new ReviewCreateRequest(5, 1L, "title", "content");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findByUserName("User")).thenReturn(Optional.of(foundUser));
        when(planRepository.findById(request.getPlanId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.write(request, null, "User"));
        assertThat(ErrorCode.PLAN_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 플래너 없음")
    void reviewWrite_fail3() throws IOException {
        // then
        ReviewCreateRequest request = new ReviewCreateRequest(5, 1L, "title", "content");
        User foundUser = User.builder().build();
        Plan foundPlan = Plan.builder().planner(Planner.builder().id(99L).starSum(1).build()).build();

        // when
        when(userRepository.findByUserName("User")).thenReturn(Optional.of(foundUser));
        when(planRepository.findById(request.getPlanId())).thenReturn(Optional.of(foundPlan));
        when(reviewRepository.save(any())).thenReturn(review);
        when(plannerRepository.findById(foundPlan.getPlanner().getId())).thenReturn(Optional.empty());

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
        AppException error = assertThrows(AppException.class, () -> reviewService.write(request, null, "User"));
        assertThat(ErrorCode.PLANNER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 작성 성공 - 파일 X")
    void reviewWrite_success1() throws IOException {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(5, 1L, "title", "content");
        User foundUser = User.builder().build();
        Plan foundPlan = Plan.builder().planner(Planner.builder().id(99L).starSum(1).build()).build();
        Planner foundPlanner = Planner.builder().starSum(1).reviewCount(0).build();

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(foundUser));
        when(planRepository.findById(1L)).thenReturn(Optional.of(foundPlan));
        when(reviewRepository.save(any())).thenReturn(review);
        when(plannerRepository.findById(foundPlan.getPlanner().getId())).thenReturn(Optional.of(foundPlanner));

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
        ReviewResponse result = reviewService.write(request, null, "user");
        assertThat("title", is(result.getTitle()));
    }

    @Test
    @DisplayName("리뷰 작성 성공 - 파일 O")
    void reviewWrite_success2() throws IOException {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(5, 1L, "title", "content");
        User foundUser = User.builder().build();
        Plan foundPlan = Plan.builder().planner(Planner.builder().id(99L).starSum(1).build()).build();
        Planner foundPlanner = Planner.builder().starSum(1).reviewCount(0).build();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test file".getBytes(StandardCharsets.UTF_8));

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(foundUser));
        when(planRepository.findById(1L)).thenReturn(Optional.of(foundPlan));
        when(reviewRepository.save(any())).thenReturn(review);
        when(plannerRepository.findById(foundPlan.getPlanner().getId())).thenReturn(Optional.of(foundPlanner));

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
        ReviewResponse result = reviewService.write(request, file, "user");
        assertThat("title", is(result.getTitle()));
    }

    @Test
    @DisplayName("리뷰 수정 실패 -  리뷰 없음")
    void reviewUpdate_fail1() {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, null);

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.update(1L, request, null, "user"));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 수정 실패 -  작성자 불일치")
    void reviewUpdate_fail2() {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, null);
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image(null)
                .build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.update(1L, request, null, "otherUser"));
        assertThat(ErrorCode.INVALID_PERMISSION, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 수정 실패 -  플래너 없음")
    void reviewUpdate_fail3() {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, null);
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image(null)
                .build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(review.getPlanner()).thenReturn(Planner.builder().id(99L).build());
        when(plannerRepository.findById(99L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.update(1L, request, null, "user"));
        assertThat(ErrorCode.PLANNER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 수정 성공 -  기존 파일 X, 수정 파일 X")
    void reviewUpdate_success1() throws IOException {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, null);
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image(null)
                .build();
        Planner foundPlanner = Planner.builder().starSum(5).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(foundPlanner));

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().id(1L).starSum(5).user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        ReviewResponse updatedReview = reviewService.update(1L, request, null, "user");
        assertThat("title", is(updatedReview.getTitle()));
    }

    @Test
    @DisplayName("리뷰 수정 성공 -  기존 파일 O, 수정 파일 X")
    void reviewUpdate_success2() throws IOException {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, "unchanged");
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image("testImage")
                .build();
        Planner foundPlanner = Planner.builder().starSum(5).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(foundPlanner));

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().id(1L).starSum(5).user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        ReviewResponse updatedReview = reviewService.update(1L, request, null, "user");
        assertThat("title", is(updatedReview.getTitle()));
    }

    @Test
    @DisplayName("리뷰 수정 성공 -  기존 파일 X, 수정 파일 O")
    void reviewUpdate_success3() throws IOException {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, "changed");
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image(null)
                .build();
        Planner foundPlanner = Planner.builder().starSum(5).build();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test file".getBytes(StandardCharsets.UTF_8));


        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(foundPlanner));

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().id(1L).starSum(5).user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        ReviewResponse updatedReview = reviewService.update(1L, request, file, "user");
        assertThat("title", is(updatedReview.getTitle()));
    }

    @Test
    @DisplayName("리뷰 수정 성공 -  기존 파일 O, 수정 파일 O")
    void reviewUpdate_success4() throws IOException {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, "changed");
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image("image")
                .build();
        Planner foundPlanner = Planner.builder().starSum(5).build();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test file".getBytes(StandardCharsets.UTF_8));


        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(foundPlanner));

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().id(1L).starSum(5).user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        ReviewResponse updatedReview = reviewService.update(1L, request, file, "user");
        assertThat("title", is(updatedReview.getTitle()));
    }

    @Test
    @DisplayName("리뷰 수정 성공 -  기존 파일 O, 파일 삭제")
    void reviewUpdate_success5() throws IOException {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest("UpdatedTitle", "UpdatedContent", 1, "changed");
        Review foundReview = Review.builder()
                .star(5)
                .user(User.builder().userName("user").build())
                .image("image")
                .build();
        Planner foundPlanner = Planner.builder().starSum(5).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(foundReview)).thenReturn(review);
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(foundPlanner));

        when(review.getId()).thenReturn(1L);
        when(review.getPlanner()).thenReturn(new Planner().builder().id(1L).starSum(5).user(new User().builder().nickname("testPlanner").build()).build());
        when(review.getUser()).thenReturn(new User().builder().nickname("user").build());
        when(review.getTitle()).thenReturn("title");
        when(review.getContent()).thenReturn("content");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(review.getStar()).thenReturn(5);
        when(review.getViews()).thenReturn(2);

        // then
        ReviewResponse updatedReview = reviewService.update(1L, request, null, "user");
        assertThat("title", is(updatedReview.getTitle()));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰 없음")
    void reviewDelete_fail1() {
        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.delete(1L, "user"));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 없는 유저")
    void reviewDelete_fail2() {
        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.delete(1L, "user"));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 작성자 불일치, 관리자 아님")
    void reviewDelete_fail3() {
        // given
        User foundOtherUser = User.builder().build();
        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(foundOtherUser));

        when(review.getUser()).thenReturn(User.builder().userName("user").build());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.delete(1L, "otherUser"));
        assertThat(ErrorCode.INVALID_PERMISSION, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 플래너 없음")
    void reviewDelete_fail4() {
        // given
        User foundUser = User.builder().build();
        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(foundUser));
        when(plannerRepository.findById(1L)).thenReturn(Optional.empty());

        when(review.getUser()).thenReturn(User.builder().userName("user").build());
        when(review.getPlanner()).thenReturn(Planner.builder().id(1L).build());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.delete(1L, "user"));
        assertThat(ErrorCode.PLANNER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 작성자 일치, 파일 존재")
    void reviewDelete_success1() {
        // given
        User foundUser = User.builder().build();
        Planner planner = Planner.builder().id(1L).starSum(5).reviewCount(2).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(foundUser));
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(planner));

        when(review.getUser()).thenReturn(User.builder().userName("user").build());
        when(review.getPlanner()).thenReturn(planner);
        when(review.getImage()).thenReturn("image");
        when(review.getStar()).thenReturn(1);
        when(review.getId()).thenReturn(1L);

        // then
        Long deletedReviewId = reviewService.delete(1L, "user");
        assertThat(1L, is(deletedReviewId));
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 작성자 불일치, 관리자, 파일 없음")
    void reviewDelete_success2() {
        // given
        User foundOtherUser = User.builder().role(UserRole.ADMIN).build();
        Planner planner = Planner.builder().id(1L).starSum(5).reviewCount(2).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(foundOtherUser));
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(planner));

        when(review.getUser()).thenReturn(User.builder().userName("user").build());
        when(review.getPlanner()).thenReturn(planner);
        when(review.getImage()).thenReturn(null);
        when(review.getStar()).thenReturn(1);
        when(review.getId()).thenReturn(1L);

        // then
        Long deletedReviewId = reviewService.delete(1L, "otherUser");
        assertThat(1L, is(deletedReviewId));
    }

    @Test
    @DisplayName("리뷰 리스트 출력 성공 - 검색어 없음")
    void readReviewList_success1() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Page<Review> reviewPage = new PageImpl<>(reviewList, pageable, 1);

        // when
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        when(review.getTitle()).thenReturn("title");
        when(review.getUser()).thenReturn(User.builder().nickname("userNick").build());
        when(review.getPlanner()).thenReturn(Planner.builder().user(User.builder().nickname("PlannerNick").build()).build());
        when(review.getId()).thenReturn(1L);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());

        // then
        Page<ReviewListResponse> result = reviewService.reviewList(pageable, null, null);
        assertThat(1L, is(result.getTotalElements()));
    }

    @Test
    @DisplayName("리뷰 리스트 출력 성공 - 검색 타입 : 제목")
    void readReviewList_success2() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Page<Review> reviewPage = new PageImpl<>(reviewList, pageable, 1);

        // when
        when(reviewRepository.findAllByTitleContains("title", pageable)).thenReturn(reviewPage);

        when(review.getTitle()).thenReturn("title");
        when(review.getUser()).thenReturn(User.builder().nickname("userNick").build());
        when(review.getPlanner()).thenReturn(Planner.builder().user(User.builder().nickname("PlannerNick").build()).build());
        when(review.getId()).thenReturn(1L);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());

        // then
        Page<ReviewListResponse> result = reviewService.reviewList(pageable, "TITLE", "title");
        assertThat(1L, is(result.getTotalElements()));
    }

    @Test
    @DisplayName("리뷰 리스트 출력 성공 - 검색 타입 : 플래너 닉네임")
    void readReviewList_success3() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Page<Review> reviewPage = new PageImpl<>(reviewList, pageable, 1);

        // when
        when(reviewRepository.findAllByPlannerUserNicknameContains("PlannerNick", pageable)).thenReturn(reviewPage);

        when(review.getTitle()).thenReturn("title");
        when(review.getUser()).thenReturn(User.builder().nickname("userNick").build());
        when(review.getPlanner()).thenReturn(Planner.builder().user(User.builder().nickname("PlannerNick").build()).build());
        when(review.getId()).thenReturn(1L);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());

        // then
        Page<ReviewListResponse> result = reviewService.reviewList(pageable, "NICKNAME", "PlannerNick");
        assertThat(1L, is(result.getTotalElements()));
    }

    @Test
    @DisplayName("리뷰 하나 찾기 실패 - 리뷰 없음")
    void findOneReview_fail() {
        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> reviewService.findById(1L, true));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("리뷰 하나 찾기 성공 - 조회수 추가 X")
    void findOneReview_success1() {
        // given
        Review foundReview = Review.builder().views(1).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));

        // then
        Review result = reviewService.findById(1L, false);
        assertThat(1, is(result.getViews()));
    }

    @Test
    @DisplayName("리뷰 하나 찾기 성공 - 조회수 추가 O")
    void findOneReview_success2() {
        // given
        Review foundReview = Review.builder().views(1).build();

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));

        // then
        Review result = reviewService.findById(1L, true);
        assertThat(2, is(result.getViews()));
    }

}