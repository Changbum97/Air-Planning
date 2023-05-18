package com.example.airplanning.service;

import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LikeServiceTest {

    LikeService likeService;

    LikeRepository likeRepository = mock(LikeRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    BoardRepository boardRepository = mock(BoardRepository.class);
    PlannerRepository plannerRepository = mock(PlannerRepository.class);
    ReviewRepository reviewRepository = mock(ReviewRepository.class);
    static User user1;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(likeRepository, userRepository, boardRepository, plannerRepository, reviewRepository);
        user1 = User.builder().id(1L).userName("user1").nickname("nick1").role(UserRole.USER).build();
    }

    @Test
    @DisplayName("좋아요 성공 Test 1 - 게시판 좋아요 추가")
    void likeSuccess1() {
        Board board1 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").category(Category.FREE).views(0).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(likeRepository.findByBoardIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        String result = assertDoesNotThrow(() -> likeService.changeLike(board1.getId(), user1.getUserName(), LikeType.BOARD_LIKE));
        assertEquals(result, "좋아요가 추가되었습니다.");
    }

    @Test
    @DisplayName("좋아요 성공 Test 2 - 게시판 좋아요 취소")
    void likeSuccess2() {
        Board board1 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").category(Category.FREE).views(0).build();
        Like like1 = Like.builder().id(1L).likeType(LikeType.BOARD_LIKE).user(user1).board(board1).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(likeRepository.findByBoardIdAndUserId(1L, 1L)).thenReturn(Optional.of(like1));

        String result = assertDoesNotThrow(() -> likeService.changeLike(board1.getId(), user1.getUserName(), LikeType.BOARD_LIKE));
        assertEquals(result, "좋아요가 취소되었습니다.");
    }

    @Test
    @DisplayName("좋아요 성공 Test 3 - 플래너 좋아요 추가")
    void likeSuccess3() {
        Planner planner1 = Planner.builder().id(1L).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(planner1));
        when(likeRepository.findByPlannerIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        String result = assertDoesNotThrow(() -> likeService.changeLike(planner1.getId(), user1.getUserName(), LikeType.PLANNER_LIKE));
        assertEquals(result, "좋아요가 추가되었습니다.");
    }

    @Test
    @DisplayName("좋아요 성공 Test 4 - 플래너 좋아요 취소")
    void likeSuccess4() {
        Planner planner1 = Planner.builder().id(1L).build();
        Like like1 = Like.builder().id(1L).likeType(LikeType.PLANNER_LIKE).user(user1).planner(planner1).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(plannerRepository.findById(1L)).thenReturn(Optional.of(planner1));
        when(likeRepository.findByPlannerIdAndUserId(1L, 1L)).thenReturn(Optional.of(like1));

        String result = assertDoesNotThrow(() -> likeService.changeLike(planner1.getId(), user1.getUserName(), LikeType.PLANNER_LIKE));
        assertEquals(result, "좋아요가 취소되었습니다.");
    }

    @Test
    @DisplayName("좋아요 성공 Test 5 - 리뷰 좋아요 추가")
    void likeSuccess5() {
        Review review1 = Review.builder().id(1L).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review1));
        when(likeRepository.findByPlannerIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        String result = assertDoesNotThrow(() -> likeService.changeLike(review1.getId(), user1.getUserName(), LikeType.REVIEW_LIKE));
        assertEquals(result, "좋아요가 추가되었습니다.");
    }

    @Test
    @DisplayName("좋아요 성공 Test 6 - 리뷰 좋아요 취소")
    void likeSuccess6() {
        Review review1 = Review.builder().id(1L).build();
        Like like1 = Like.builder().id(1L).likeType(LikeType.REVIEW_LIKE).user(user1).review(review1).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review1));
        when(likeRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.of(like1));

        String result = assertDoesNotThrow(() -> likeService.changeLike(review1.getId(), user1.getUserName(), LikeType.REVIEW_LIKE));
        assertEquals(result, "좋아요가 취소되었습니다.");
    }

    @Test
    @DisplayName("좋아요 실패 Test 1 - 유저가 없는 경우")
    void likeFail1() {
        Board board1 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").category(Category.FREE).views(0).build();
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () ->
                likeService.changeLike(board1.getId(), user1.getUserName(), LikeType.BOARD_LIKE));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 실패 Test 2 - 게시글 없는 경우")
    void likeFail2() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () ->
                likeService.changeLike(1L, user1.getUserName(), LikeType.BOARD_LIKE));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 실패 Test 3 - 플래너가 없는 경우")
    void likeFail3() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(plannerRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () ->
                likeService.changeLike(1L, user1.getUserName(), LikeType.PLANNER_LIKE));
        assertEquals(ErrorCode.PLANNER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 실패 Test 4 - 리뷰가 없는 경우")
    void likeFail4() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () ->
                likeService.changeLike(1L, user1.getUserName(), LikeType.REVIEW_LIKE));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 체크 성공 Test 1 - 게시판 좋아요가 없는 경우")
    void checkLikeSuccess1() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(likeRepository.existsByBoardIdAndUserId(1L, 1L)).thenReturn(false);

        boolean result = assertDoesNotThrow(() -> likeService.checkLike(1L, user1.getUserName()));
        assertEquals(result, false);
    }

    @Test
    @DisplayName("좋아요 체크 성공 Test 2 - 게시판 좋아요가 있는 경우")
    void checkLikeSuccess2() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(likeRepository.existsByBoardIdAndUserId(1L, 1L)).thenReturn(true);

        boolean result = assertDoesNotThrow(() -> likeService.checkLike(1L, user1.getUserName()));
        assertEquals(result, true);
    }

    @Test
    @DisplayName("좋아요 체크 성공 Test 3 - 플래너 좋아요 체크")
    void checkLikeSuccess3() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(likeRepository.existsByPlannerIdAndUserId(1L, 1L)).thenReturn(true);

        boolean result = assertDoesNotThrow(() -> likeService.checkPlannerLike(1L, user1.getUserName()));
        assertEquals(result, true);
    }

    @Test
    @DisplayName("좋아요 체크 성공 Test 4 - 리뷰 좋아요 체크")
    void checkLikeSuccess4() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(likeRepository.existsByReviewIdAndUserId(1L, 1L)).thenReturn(true);

        boolean result = assertDoesNotThrow(() -> likeService.checkReviewLike(1L, user1.getUserName()));
        assertEquals(result, true);
    }

    @Test
    @DisplayName("좋아요 체크 실패 Test 1 - 게시판 좋아요 체크 시 유저가 없는 경우")
    void checkLikeFail1() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> likeService.checkLike(1L, "user1"));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 체크 실패 Test 2 - 플래너 좋아요 체크 시 유저가 없는 경우")
    void checkLikeFail2() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> likeService.checkPlannerLike(1L, "user1"));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 체크 실패 Test 3 - 리뷰 좋아요 체크 시 유저가 없는 경우")
    void checkLikeFail3() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> likeService.checkReviewLike(1L, "user1"));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }
}