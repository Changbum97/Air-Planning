package com.example.airplanning.service;

import com.example.airplanning.domain.dto.comment.*;
import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.CommentRepository;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CommentServiceTest {
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final AlarmService alarmService = mock(AlarmService.class);

    CommentService commentService;

    @BeforeEach
    void beforeEach() {
        commentService = new CommentService(commentRepository, userRepository, boardRepository, reviewRepository, alarmService);
    }

    @Test
    @DisplayName("댓글 작성 실패 -  유저 없음")
    void createComment_fail1 () {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "comment", "board");

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createComment(request, 1L));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 게시글 없음")
    void createComment_fail2 () {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "comment", "board");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(boardRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createComment(request, 1L));
        assertThat(ErrorCode.BOARD_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 리뷰 없음")
    void createComment_fail3 () {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "comment", "review");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(reviewRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createComment(request, 1L));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 작성 성공 - 자유 게시판")
    void createComment_success1() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "comment", "board");
        User foundUser = User.builder().id(1L).userName("user").nickname("userNick").build();
        Board foundBoard = Board.builder().build();
        List<Comment> coCo = new ArrayList<>();
        Comment savedComment = Comment.builder()
                .id(1L)
                .content("content")
                .user(foundUser)
                .children(coCo)
                .deletedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(savedComment, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(boardRepository.findById(request.getPostId())).thenReturn(Optional.of(foundBoard));
        when(commentRepository.save(any())).thenReturn(savedComment);

        // then
        CommentResponse result = commentService.createComment(request, 1L);
        assertThat("content", is(result.getContent()));
    }

    @Test
    @DisplayName("댓글 작성 성공 - 리뷰")
    void createComment_success2() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "comment", "review");
        User foundUser = User.builder().id(1L).userName("user").nickname("userNick").build();
        Review foundReview = Review.builder().build();
        Comment savedComment = Comment.builder()
                .id(1L)
                .content("content")
                .user(foundUser)
                .build();
        ReflectionTestUtils.setField(savedComment, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(reviewRepository.findById(request.getPostId())).thenReturn(Optional.of(foundReview));
        when(commentRepository.save(any())).thenReturn(savedComment);

        // then
        CommentResponse result = commentService.createComment(request, 1L);
        assertThat("content", is(result.getContent()));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 유저 없음")
    void updateComment_fail1() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "content", "board");

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.updateComment(request, 1L));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 게시글 없음")
    void updateComment_fail2() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "content", "board");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.updateComment(request, 1L));
        assertThat(ErrorCode.BOARD_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 리뷰 없음")
    void updateComment_fail3() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "content", "review");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.updateComment(request, 1L));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 없음")
    void updateComment_fail4() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "content", "board");
        User foundUser = User.builder().build();
        Board foundBoard = Board.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(foundBoard));
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.updateComment(request, 1L));
        assertThat(ErrorCode.COMMENT_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 아님")
    void updateComment_fail5() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "content", "review");
        User foundUser = User.builder().id(1L).build();
        Review foundReview = Review.builder().build();
        Comment targetComment = Comment.builder().user(User.builder().id(2L).build()).build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(targetComment));

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.updateComment(request, 1L));
        assertThat(ErrorCode.INVALID_PERMISSION, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(1L, 1L, "updatedContent", "board");
        User foundUser = User.builder().id(1L).userName("user").nickname("userNick").build();
        Board foundBoard = Board.builder().build();
        Comment targetComment = Comment.builder().user(foundUser).build();
        Comment updatedComment = Comment.builder()
                .id(1L)
                .content("updatedContent")
                .user(foundUser)
                .build();
        ReflectionTestUtils.setField(updatedComment, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(foundBoard));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(targetComment));
        when(commentRepository.save(targetComment)).thenReturn(updatedComment);

        // then
        CommentResponse result = commentService.updateComment(request, 1L);
        assertThat("updatedContent", is(result.getContent()));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 유저 없음")
    void deleteComment_fail1() {
        // given
        CommentDeleteRequest request = new CommentDeleteRequest(1L, 1L);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.deleteComment(request, 1L));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 없음")
    void deleteComment_fail2() {
        // given
        CommentDeleteRequest request = new CommentDeleteRequest(1L, 1L);
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.deleteComment(request, 1L));
        assertThat(ErrorCode.COMMENT_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자 아님 + 권한 없음")
    void deleteComment_fail3() {
        // given
        CommentDeleteRequest request = new CommentDeleteRequest(1L, 1L);
        User foundUser = User.builder().id(1L).role(UserRole.USER).build();
        Comment targetComment = Comment.builder().user(User.builder().id(2L).build()).build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(targetComment));

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.deleteComment(request, 1L));
        assertThat(ErrorCode.INVALID_PERMISSION, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 작성자, 부모 댓글 없음, 자식 댓글 없음")
    void deleteComment_success1() {
        // given
        CommentDeleteRequest request = new CommentDeleteRequest(1L, 1L);
        User foundUser = User.builder().id(1L).role(UserRole.USER).build();
        List<Comment> children = spy(new ArrayList<>());
        Comment targetComment = Comment.builder().user(foundUser).children(children).build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(targetComment));

        // then
        Long result = commentService.deleteComment(request, 1L);
        assertThat(-1L, is(result));
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 관리자, 부모 댓글 있음, 자식 댓글 있음")
    void deleteComment_success2() {
        // given
        CommentDeleteRequest request = new CommentDeleteRequest(1L, 1L);
        User foundUser = User.builder().id(1L).role(UserRole.ADMIN).build();
        Comment parentComment = Comment.builder().id(99L).build();
        List<Comment> children = new ArrayList<>();
        children.add(Comment.builder().id(100L).build());
        Comment targetComment = Comment.builder()
                .user(User.builder().id(2L).build())
                .parent(parentComment)
                .children(children)
                .build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(targetComment));

        // then
        Long result = commentService.deleteComment(request, 1L);
        assertThat(99L, is(result));
        assertThat("삭제된 댓글입니다.", is(targetComment.getContent()));
    }

    @Test
    @DisplayName("부모 댓글 지우기 실패 - 댓글 없음")
    void deleteParent_fail() {
        // when
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.deleteParent(1L));
        assertThat(ErrorCode.COMMENT_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("부모 댓글 지우기 성공 - 댓글 없음")
    void deleteParent_success() {
        ByteArrayOutputStream outputStreamCaptor;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // given
        List<Comment> children = spy(new ArrayList<>());
        Comment parentComment = Comment.builder().deletedAt(LocalDateTime.now()).children(children).build();

        // when
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));

        // then
        commentService.deleteParent(1L);
        assertThat("지운다.\r\n", is(outputStreamCaptor.toString()));
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 유저 없음")
    void createCoComment_fail1() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "board");

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createCoComment(request, 1L));
        assertThat(ErrorCode.USER_NOT_FOUNDED, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 부모 댓글 없음")
    void createCoComment_fail2() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "board");
        User foundUser = User.builder().build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createCoComment(request, 1L));
        assertThat(ErrorCode.COMMENT_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 게시글 없음")
    void createCoComment_fail3() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "board");
        User foundUser = User.builder().build();
        Comment parentComment = Comment.builder().id(1L).build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));
        when(boardRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createCoComment(request, 1L));
        assertThat(ErrorCode.BOARD_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 리뷰 없음")
    void createCoComment_fail4() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "review");
        User foundUser = User.builder().build();
        Comment parentComment = Comment.builder().id(1L).build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));
        when(reviewRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.createCoComment(request, 1L));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 작성 성공 - 게시글")
    void createCoComment_success1() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "board");
        User foundUser = User.builder().id(1L).userName("user").nickname("userNick").build();
        Comment parentComment = Comment.builder().id(1L).build();
        Board foundBoard = Board.builder().build();
        Comment coCo = Comment.builder()
                .id(2L)
                .content("CoContent")
                .user(foundUser)
                .parent(parentComment)
                .build();
        ReflectionTestUtils.setField(coCo, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));
        when(boardRepository.findById(request.getPostId())).thenReturn(Optional.of(foundBoard));
        when(commentRepository.save(any())).thenReturn(coCo);

        // then
        CommentResponse result = commentService.createCoComment(request, 1L);
        assertThat("CoContent", is(result.getContent()));
    }

    @Test
    @DisplayName("대댓글 작성 성공 - 리뷰")
    void createCoComment_success2() {
        // given
        CoCommentCreateRequest request = new CoCommentCreateRequest(1L, 1L, "CoContent", "review");
        User foundUser = User.builder().id(1L).userName("user").nickname("userNick").build();
        Comment parentComment = Comment.builder().id(1L).build();
        Review foundReview = Review.builder().build();
        Comment coCo = Comment.builder()
                .id(2L)
                .content("CoContent")
                .user(foundUser)
                .parent(parentComment)
                .build();
        ReflectionTestUtils.setField(coCo, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(foundUser));
        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));
        when(reviewRepository.findById(request.getPostId())).thenReturn(Optional.of(foundReview));
        when(commentRepository.save(any())).thenReturn(coCo);

        // then
        CommentResponse result = commentService.createCoComment(request, 1L);
        assertThat("CoContent", is(result.getContent()));
    }

    @Test
    @DisplayName("댓글 목록 읽기 실패 - 게시글 없음")
    void readComments_fail1() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.readComment(1L, "board", pageable));
        assertThat(ErrorCode.BOARD_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 목록 읽기 실패 - 리뷰 없음")
    void readComments_fail2() {
        // given
        Pageable pageable = PageRequest.of(0,10);

        // when
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> commentService.readComment(1L, "review", pageable));
        assertThat(ErrorCode.REVIEW_NOT_FOUND, is(error.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 목록 읽기 성공 - 게시글")
    void readComments_success1() {
        // given
        Pageable pageable = PageRequest.of(0,10);
        Board foundBoard = Board.builder().build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .content("content1")
                .user(User.builder().id(1L).userName("user").nickname("userNick").build())
                .build();
        ReflectionTestUtils.setField(comment1, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);

        Page<Comment> commentPage = new PageImpl<>(commentList, pageable, 1);

        // when
        when(boardRepository.findById(1L)).thenReturn(Optional.of(foundBoard));
        when(commentRepository.findByBoardAndParentIsNull(foundBoard, pageable)).thenReturn(commentPage);

        // then
        Page<CommentResponse> result = commentService.readComment(1L, "board", pageable);
        assertThat("content1", is(result.getContent().get(0).getContent()));
    }

    @Test
    @DisplayName("댓글 목록 읽기 성공 - 리뷰")
    void readComments_success2() {
        // given
        Pageable pageable = PageRequest.of(0,10);
        Review foundReview = Review.builder().build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .content("content1")
                .user(User.builder().id(1L).userName("user").nickname("userNick").build())
                .build();
        ReflectionTestUtils.setField(comment1, BaseEntity.class, "createdAt", LocalDateTime.now(), LocalDateTime.class);

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);

        Page<Comment> commentPage = new PageImpl<>(commentList, pageable, 1);

        // when
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(foundReview));
        when(commentRepository.findByReviewAndParentIsNull(foundReview, pageable)).thenReturn(commentPage);

        // then
        Page<CommentResponse> result = commentService.readComment(1L, "review", pageable);
        assertThat("content1", is(result.getContent().get(0).getContent()));
    }

}