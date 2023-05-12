package com.example.airplanning.service;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.CommentType;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class MyPageServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final LikeRepository likeRepository = mock(LikeRepository.class);
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final PlanRepository planRepository = mock(PlanRepository.class);
    MyPageService myPageService;

    @BeforeEach
    void beforeEach() {
        myPageService = new MyPageService(userRepository, boardRepository, commentRepository, likeRepository, reviewRepository, planRepository);
    }

    @Test
    @DisplayName("내 정보 불러오기 실패 - 유저 정보 없음")
    void getMyPageInfo_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getMyPageInfo("user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내 정보 불러오기 성공")
    void getMyPageInfo_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        // then
        MyPageInfoResponse result = myPageService.getMyPageInfo("user");
        assertThat(result.getNickname(), is("testNick"));
    }

    @Test
    @DisplayName("내가 쓴 게시글 목록 호출 실패 - 유저 정보 없음")
    void getMyBoard_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getMyBoard( pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 쓴 게시글 목록 호출 성공")
    void getMyBoard_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Board> list = new ArrayList<>();
        Board board1 = Board.builder().title("test1").category(Category.FREE).build();
        ReflectionTestUtils.setField(board1, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(board1);
        Page<Board> page = new PageImpl<>(list);


        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(boardRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPageBoardResponse> result = myPageService.getMyBoard(pageable, "user");
        assertThat(result.getContent().get(0).getTitle(), is("test1"));
    }

    @Test
    @DisplayName("내가 쓴 리뷰 목록 호출 실패 - 유저 정보 없음")
    void getMyReview_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getMyReview( pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 쓴 리뷰 목록 호출 성공")
    void getMyReview_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Review> list = new ArrayList<>();
        Review review = Review.builder().title("test1").planner(Planner.builder().user(User.builder().nickname("nick").build()).build()).build();
        ReflectionTestUtils.setField(review, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(review);
        Page<Review> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(reviewRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPageReviewResponse> result = myPageService.getMyReview(pageable, "user");
        assertThat(result.getContent().get(0).getTitle(), is("test1"));
    }

    @Test
    @DisplayName("내가 쓴 댓글 목록 호출 실패 - 유저 정보 없음")
    void getMyComment_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getMyComment( pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 쓴 댓글 목록 호출 성공")
    void getMyComment_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Comment> list = new ArrayList<>();
        Comment comment = Comment.builder()
                .id(1L)
                .content("testCo")
                .commentType(CommentType.BOARD_COMMENT)
                .board(Board.builder().id(2L).title("testTitle").build())
                .build();
        ReflectionTestUtils.setField(comment, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(comment);
        Page<Comment> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(commentRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPageCommentResponse> result = myPageService.getMyComment(pageable, "user");
        assertThat(result.getContent().get(0).getContent(), is("testCo"));
    }

    @Test
    @DisplayName("내가 좋아요 한 게시글 목록 호출 실패 - 유저 정보 없음")
    void getLikeBoard_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getLikeBoard(pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 좋아요 한 게시글 목록 호출 성공")
    void getLikeBoard_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Like> list = new ArrayList<>();
        Like like = Like.builder()
                .id(1L)
                .likeType(LikeType.BOARD_LIKE)
                .board(Board.builder().title("testTitle").category(Category.FREE).build())
                .build();
        ReflectionTestUtils.setField(like, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(like);
        Page<Like> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(likeRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPageBoardResponse> result = myPageService.getLikeBoard(pageable, "user");
        assertThat(result.getContent().get(0).getTitle(), is("testTitle"));
    }

    @Test
    @DisplayName("내가 좋아요 한 리뷰 목록 호출 실패 - 유저 정보 없음")
    void getLikeReview_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getLikeReview(pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 좋아요 한 리뷰 목록 호출 성공")
    void getLikeReview_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Like> list = new ArrayList<>();
        Like like = Like.builder()
                .id(1L)
                .likeType(LikeType.REVIEW_LIKE)
                .review(Review.builder().title("testReview").planner(Planner.builder().user(User.builder().nickname("testNick").build()).build()).build())
                .build();
        ReflectionTestUtils.setField(like, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(like);
        Page<Like> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(likeRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPageReviewResponse> result = myPageService.getLikeReview(pageable, "user");
        assertThat(result.getContent().get(0).getTitle(), is("testReview"));
    }

    @Test
    @DisplayName("내가 좋아요 한 플래너 목록 호출 실패 - 유저 정보 없음")
    void getLikePlanner_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getLikePlanner(pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("내가 좋아요 한 플래너 목록 호출 성공")
    void getLikePlanner_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Like> list = new ArrayList<>();
        Like like = Like.builder()
                .id(1L)
                .likeType(LikeType.PLANNER_LIKE)
                .planner(Planner.builder().user(User.builder().nickname("testNick").build()).starSum(5).reviewCount(1).build())
                .build();
        ReflectionTestUtils.setField(like, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(like);
        Page<Like> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(likeRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPagePlannerResponse> result = myPageService.getLikePlanner(pageable, "user");
        assertThat(result.getContent().get(0).getPlannerName(), is("testNick"));
    }

    @Test
    @DisplayName("여행중인 플랜 목록 호출 실패 - 유저 정보 없음")
    void getProgressPlan_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getProgressPlan(pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("'여행중'인 플랜 목록 호출 성공")
    void getProgressPlan_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Plan> list = new ArrayList<>();
        Plan plan1 = Plan.builder().planType(PlanType.ACCEPT).title("title1").planner(Planner.builder().id(1L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan1, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan2 = Plan.builder().planType(PlanType.WAITING).title("title2").planner(Planner.builder().id(2L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan2, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan3 = Plan.builder().planType(PlanType.REFUSE).title("title3").planner(Planner.builder().id(3L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan3, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan4 = Plan.builder().planType(PlanType.COMPLETE).title("title4").planner(Planner.builder().id(4L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan4, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(plan1);
        list.add(plan2);
        list.add(plan3);
        list.add(plan4);
        Page<Plan> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(planRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPagePlanResponse> result = myPageService.getProgressPlan(pageable, "user");
        assertThat(result.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("여행완료인 플랜 목록 호출 실패 - 유저 정보 없음")
    void getFinishPlan_fail() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> myPageService.getFinishPlan(pageable, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("여행완료인 플랜 목록 호출 성공")
    void getFinishPlan_success() {
        // given
        User user = spy(User.builder().nickname("testNick").build());
        Pageable pageable = PageRequest.of(0,10);
        List<Plan> list = new ArrayList<>();
        Plan plan1 = Plan.builder().planType(PlanType.ACCEPT).title("title1").planner(Planner.builder().id(1L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan1, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan2 = Plan.builder().planType(PlanType.WAITING).title("title2").planner(Planner.builder().id(2L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan2, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan3 = Plan.builder().planType(PlanType.REFUSE).title("title3").planner(Planner.builder().id(3L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan3, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        Plan plan4 = Plan.builder().planType(PlanType.COMPLETE).title("title4").planner(Planner.builder().id(4L).build()).isReviewed(false).build();
        ReflectionTestUtils.setField(plan4, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);
        list.add(plan1);
        list.add(plan2);
        list.add(plan3);
        list.add(plan4);
        Page<Plan> page = new PageImpl<>(list);

        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(planRepository.findAllByUser(user, pageable)).thenReturn(page);

        // then
        Page<MyPagePlanResponse> result = myPageService.getFinishPlan(pageable, "user");
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent().get(0).getTitle(), is("title4"));
    }

}