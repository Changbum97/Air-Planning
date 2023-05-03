package com.example.airplanning.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardDto;
import com.example.airplanning.domain.dto.board.BoardListResponse;
import com.example.airplanning.domain.dto.board.BoardUpdateRequest;
import com.example.airplanning.domain.entity.BaseEntity;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.RegionRepository;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BoardServiceTest {
    BoardService boardService;

    BoardRepository boardRepository = mock(BoardRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    RegionRepository regionRepository = mock(RegionRepository.class);
    AlarmService alarmService = mock(AlarmService.class);
    AmazonS3 amazonS3 = mock(AmazonS3.class);

    private static User user1, admin;
    private static Region region1;
    private static Board board1, board2;

    @BeforeEach
    void setUp() {
        boardService = new BoardService(boardRepository, userRepository, regionRepository, alarmService, amazonS3);
        user1 = User.builder().id(1L).userName("user1").nickname("nick1").role(UserRole.USER).build();
        admin = User.builder().id(2L).userName("admin").nickname("관리자").role(UserRole.ADMIN).build();
        region1 = Region.builder().id(30L).region1("경기도").region2("수원시").build();
        board1 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").category(Category.FREE).views(0).build();
        board2 = Board.builder().id(2L).user(user1).title("rankUp").content("내용2").category(Category.RANK_UP).region(region1).amount(3000).build();
    }

    @Test
    @DisplayName("글 조회 성공 Test 1")
    void detailSuccess1() {
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(
                board1,
                BaseEntity.class,
                "createdAt",
                now,
                LocalDateTime.class);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));

        BoardDto dto = assertDoesNotThrow(() -> boardService.detail(1L, false, Category.FREE));
        assertEquals(board1.getId(), dto.getId());
        assertEquals(board1.getUser().getUserName(), dto.getUserName());
        assertEquals(board1.getUser().getNickname(), dto.getNickname());
        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
        assertEquals(board1.getCategory(), dto.getCategory());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    @DisplayName("글 조회 성공 Test 2 - RankUp")
    void detailSuccess2() {
        when(boardRepository.findById(2L)).thenReturn(Optional.of(board2));

        BoardDto dto = assertDoesNotThrow(() -> boardService.detail(2L, false, Category.RANK_UP));
        assertEquals(board2.getId(), dto.getId());
        assertEquals(board2.getUser().getUserName(), dto.getUserName());
        assertEquals(board2.getUser().getNickname(), dto.getNickname());
        assertEquals(board2.getTitle(), dto.getTitle());
        assertEquals(board2.getContent(), dto.getContent());
        assertEquals(board2.getCategory(), dto.getCategory());
        assertEquals(board2.getAmount(), dto.getAmount());
        assertEquals(board2.getRegion().getRegion1() + " " + board2.getRegion().getRegion2(), dto.getRegion());
    }

    @Test
    @DisplayName("글 조회 성공 Test 3 - 조회수 증가하는지 Test")
    void detailSuccess3() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));

        BoardDto dto = assertDoesNotThrow(() -> boardService.detail(1L, true, Category.FREE));
        assertEquals(board1.getId(), dto.getId());
        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
        assertEquals(1, dto.getViews());
    }

    @Test
    @DisplayName("글 조회 실패 Test 1 - 글이 존재하지 않는 경우")
    void detailFail1() {
        when(boardRepository.findById(100L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () ->
                boardService.detail(100L, false, Category.FREE));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 조회 실패 Test 2 - 카테고리가 일치하지 않는 경우")
    void detailFail2() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));

        AppException e = assertThrows(AppException.class, () ->
                boardService.detail(1L, false, Category.RANK_UP));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 작성 성공 Test 1")
    void writeSuccess1() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board1);

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board1.getTitle(), board1.getContent(), null, null);
        BoardDto dto = assertDoesNotThrow(() -> boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.FREE));
        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
    }

    @Test
    @DisplayName("글 작성 성공 Test 2 - RankUp")
    void writeSuccess2() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board2);
        when(regionRepository.findById(30L)).thenReturn(Optional.of(region1));

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board2.getTitle(), board2.getContent(), board2.getAmount(), board2.getRegion().getId());
        BoardDto dto = assertDoesNotThrow(() -> boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.RANK_UP));
        assertEquals(board2.getTitle(), dto.getTitle());
        assertEquals(board2.getContent(), dto.getContent());
        assertEquals(board2.getAmount(), dto.getAmount());
        assertEquals(board2.getRegion().getRegion1() + " " + board2.getRegion().getRegion2(), dto.getRegion());
    }

    @Test
    @DisplayName("글 작성 성공 Test 3 - 파일이 있는 경우")
    void writeSuccess3() {
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").content("내용3").category(Category.FREE).image("test.txt").build();
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board3);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hi".getBytes(StandardCharsets.UTF_8));
        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board3.getTitle(), board3.getContent(), null, null);
        BoardDto dto = assertDoesNotThrow(() -> boardService.writeWithFile(boardCreateRequest, file, user1.getUserName(), Category.FREE));

        assertEquals(board3.getTitle(), dto.getTitle());
        assertEquals(board3.getContent(), dto.getContent());
        assertEquals(board3.getImage(), dto.getImage());
    }

    @Test
    @DisplayName("글 작성 성공 Test 4 - RankUp 글 작성 시 ADMIN에게 알람이 전송되는지 테스트")
    void writeSuccess4() {
        List<User> admins = new ArrayList<>();
        admins.add(admin);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(admins);

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board2);
        when(regionRepository.findById(30L)).thenReturn(Optional.of(region1));

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board2.getTitle(), board2.getContent(), board2.getAmount(), board2.getRegion().getId());
        assertDoesNotThrow(() -> boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.RANK_UP));
        verify(alarmService).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("글 작성 성공 Test 5 - Report 글 작성 시 ADMIN에게 알람이 전송되는지 테스트")
    void writeSuccess5() {
        List<User> admins = new ArrayList<>();
        admins.add(admin);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(admins);

        User user2 = User.builder().id(2L).userName("user2").nickname("nick2").role(UserRole.USER).build();
        Board board3 = Board.builder().id(3L).user(user1).title("nick2").content("신고").category(Category.REPORT).build();
        when(boardRepository.save(any())).thenReturn(board3);
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByNickname("nick2")).thenReturn(Optional.of(user2));

        BoardCreateRequest boardCreateRequest2 = new BoardCreateRequest(board3.getTitle(), board3.getContent(), null, null);
        assertDoesNotThrow(() -> boardService.writeWithFile(boardCreateRequest2, null, user1.getUserName(), Category.REPORT));
        verify(alarmService).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("글 작성 실패 Test 1 - 유저가 없는 경우")
    void writeFail1() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());
        when(boardRepository.save(any())).thenReturn(board1);

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board1.getTitle(), board1.getContent(), null, null);

        AppException e = assertThrows(AppException.class, () ->
                boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.FREE));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("글 작성 실패 Test 2 - RankUP에서 Region이 없는 경우")
    void writeFail2() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board2);
        when(regionRepository.findById(30L)).thenReturn(Optional.empty());

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board2.getTitle(), board2.getContent(), board2.getAmount(), board2.getRegion().getId());
        AppException e = assertThrows(AppException.class, () ->
                boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.RANK_UP));
        assertEquals(ErrorCode.REGION_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 작성 실패 Test 3 - Report에서 Title에 있는 Nickname에 해당하는 유저가 존재하지 않는 경우")
    void writeFail3() {
        Board board3 = Board.builder().id(3L).user(user1).title("nick2").content("신고").category(Category.REPORT).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByNickname("nick2")).thenReturn(Optional.empty());
        when(boardRepository.save(any())).thenReturn(board3);

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board3.getTitle(), board3.getContent(), null, null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.writeWithFile(boardCreateRequest, null, user1.getUserName(), Category.REPORT));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }


    @Test
    @DisplayName("글 작성 실패 Test 4 - 파일 업로드에 실패한 경우")
    void writeFail4() {
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").content("내용3").category(Category.FREE).image("test.txt").build();
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.save(any())).thenReturn(board3);
        when(amazonS3.putObject(any(), any(), any(), any())).thenThrow(AmazonServiceException.class);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hi".getBytes(StandardCharsets.UTF_8));
        BoardCreateRequest boardCreateRequest = new BoardCreateRequest(board3.getTitle(), board3.getContent(), null, null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.writeWithFile(boardCreateRequest, file, user1.getUserName(), Category.FREE));
        assertEquals(ErrorCode.FILE_UPLOAD_ERROR, e.getErrorCode());

    }

    @Test
    @DisplayName("글 수정 성공 Test 1")
    void modifySuccess1() {
        Board modifiedBoard = Board.builder().id(1L).user(user1).title("수정된 제목").content("수정된 내용").category(Category.FREE).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, null, user1.getUserName(), board1.getId(), board1.getCategory()));

        assertEquals(modifiedBoard.getTitle(), dto.getTitle());
        assertEquals(modifiedBoard.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
    }

    @Test
    @DisplayName("글 수정 성공 Test 2 - 기존에는 파일이 없었지만 수정시에는 파일이 있는 경우")
    void modifySuccess2() {
        Board modifiedBoard = Board.builder().id(1L).user(user1).title("수정된 제목").content("수정된 내용").image("test.txt").category(Category.FREE).build();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hi".getBytes(StandardCharsets.UTF_8));

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, file, user1.getUserName(), board1.getId(), board1.getCategory()));

        assertEquals(modifiedBoard.getTitle(), dto.getTitle());
        assertEquals(modifiedBoard.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(modifiedBoard.getImage(), dto.getImage());
    }

    @Test
    @DisplayName("글 수정 성공 Test 3 - 기존에는 파일이 있었지만 수정시에 파일이 삭제된 경우")
    void modifySuccess3() {
        Board board3 = Board.builder().id(3L).user(user1).title("제목1").content("내용1").image("test.txt").category(Category.FREE).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(3L)).thenReturn(Optional.of(board3));
        when(boardRepository.save(any())).thenReturn(board1);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(board1.getTitle(), board1.getContent(), "changed");
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, null, user1.getUserName(), board3.getId(), board3.getCategory()));

        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(null, dto.getImage());
    }

    @Test
    @DisplayName("글 수정 성공 Test 4 - 기존에 파일이 있었고 수정시 파일은 그대로인 경우")
    void modifySuccess4() {
        Board board3 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").image("test.txt").category(Category.FREE).build();
        board1.modify(board1.getTitle(), board1.getContent(), "test.txt");

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board3));
        when(boardRepository.save(any())).thenReturn(board1);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(board1.getTitle(), board1.getContent(), "unChanged");
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, null, user1.getUserName(), board3.getId(), board3.getCategory()));

        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(board1.getImage(), dto.getImage());
    }

    @Test
    @DisplayName("글 수정 성공 Test 5 - 기존에 파일이 있었고 수정시 파일이 수정된 경우")
    void modifySuccess5() {
        Board board3 = Board.builder().id(1L).user(user1).title("제목1").content("내용1").image("test1.txt").category(Category.FREE).build();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hi".getBytes(StandardCharsets.UTF_8));
        board1.modify(board1.getTitle(), board1.getContent(), "test.txt");

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board3));
        when(boardRepository.save(any())).thenReturn(board1);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(board1.getTitle(), board1.getContent(), "changed");
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, file, user1.getUserName(), board3.getId(), board3.getCategory()));

        assertEquals(board1.getTitle(), dto.getTitle());
        assertEquals(board1.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(file.getOriginalFilename(), dto.getImage());
    }

    @Test
    @DisplayName("글 수정 성공 Test 6 - RankUp")
    void modifySuccess6() {
        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board modifiedBoard = Board.builder().id(2L).user(user1).title("수정된 제목").content("수정된 내용").category(Category.RANK_UP).region(region2).amount(5000).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(2L)).thenReturn(Optional.of(board2));
        when(regionRepository.findById(128L)).thenReturn(Optional.of(region2));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null, modifiedBoard.getRegion().getId(), modifiedBoard.getAmount());
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, null, user1.getUserName(), board2.getId(), board2.getCategory()));

        assertEquals(modifiedBoard.getTitle(), dto.getTitle());
        assertEquals(modifiedBoard.getContent(), dto.getContent());
        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(modifiedBoard.getRegion().getRegion1() + " " + modifiedBoard.getRegion().getRegion2(), dto.getRegion());

    }

    @Test
    @DisplayName("글 수정 성공 Test 7 - Report")
    void modifySuccess7() {
        User user2 = User.builder().id(2L).userName("user2").nickname("nick2").role(UserRole.USER).build();
        Board board3 = Board.builder().id(3L).user(user1).title("nick1").content("신고").category(Category.REPORT).build();
        Board modifiedBoard = Board.builder().id(3L).user(user1).title("nick2").content("신고").category(Category.REPORT).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByNickname("nick2")).thenReturn(Optional.of(user2));
        when(boardRepository.findById(3L)).thenReturn(Optional.of(board3));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        BoardDto dto = assertDoesNotThrow(() -> boardService.modify(boardUpdateRequest, null, user1.getUserName(), board3.getId(), board3.getCategory()));

        assertEquals(user1.getUserName(), dto.getUserName());
        assertEquals(user2.getNickname(), dto.getTitle());
        assertEquals(modifiedBoard.getContent(), dto.getContent());
    }

    @Test
    @DisplayName("글 수정 실패 Test 1 - 유저가 없는 경우")
    void modifyFail1() {
        Board modifiedBoard = Board.builder().id(1L).user(user1).title("수정된 제목").content("수정된 내용").category(Category.FREE).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.modify(boardUpdateRequest, null, user1.getUserName(), board1.getId(), board1.getCategory()));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("글 수정 실패 Test 2 - 수정하려는 글이 없는 경우")
    void modifyFail2() {
        Board modifiedBoard = Board.builder().id(1L).user(user1).title("수정된 제목").content("수정된 내용").category(Category.FREE).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.modify(boardUpdateRequest, null, user1.getUserName(), board1.getId(), board1.getCategory()));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 수정 실패 Test 3 - 작성자와 수정하려는 유저가 다른 경우")
    void modifyFail3() {
        Board modifiedBoard = Board.builder().id(1L).user(user1).title("수정된 제목").content("수정된 내용").category(Category.FREE).build();
        User user2 = User.builder().id(2L).userName("user2").nickname("nick2").role(UserRole.USER).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(user2));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.modify(boardUpdateRequest, null, "user2", board1.getId(), board1.getCategory()));
        assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

    @Test
    @DisplayName("글 수정 실패 Test 4 - RankUp에서 Region이 없는 경우")
    void modifyFail4() {
        Board modifiedBoard = Board.builder().id(2L).user(user1).title(user1.getNickname()).content("내용").category(Category.RANK_UP).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(boardRepository.findById(2L)).thenReturn(Optional.of(board2));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);
        when(regionRepository.findById(30L)).thenReturn(Optional.of(region1));

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.modify(boardUpdateRequest, null, user1.getUserName(), board2.getId(), board2.getCategory()));
        assertEquals(ErrorCode.REGION_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 수정 실패 Test 5 - Report에서 Title에 있는 Nickname에 해당하는 유저가 존재하지 않는 경우")
    void modifyFail5() {
        Board board3 = Board.builder().id(3L).user(user1).title("nick1").content("신고").category(Category.REPORT).build();
        Board modifiedBoard = Board.builder().id(3L).user(user1).title("nick2").content("신고").category(Category.REPORT).build();

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByNickname("nick2")).thenReturn(Optional.empty());
        when(boardRepository.findById(3L)).thenReturn(Optional.of(board3));
        when(boardRepository.save(any())).thenReturn(modifiedBoard);

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest(modifiedBoard.getTitle(), modifiedBoard.getContent(), null);
        AppException e = assertThrows(AppException.class, () ->
                boardService.modify(boardUpdateRequest, null, user1.getUserName(), board3.getId(), board3.getCategory()));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("글 삭제 성공 Test 1")
    void deleteSuccess1() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));

        Long deletedBoardId = assertDoesNotThrow(() -> boardService.delete("user1", 1L));
        assertEquals(1L, deletedBoardId);
    }

    @Test
    @DisplayName("글 삭제 성공 Test 2 - ADMIN이 글을 삭제한 경우")
    void deleteSuccess2() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(admin));

        Long deletedBoardId = assertDoesNotThrow(() -> boardService.delete("admin", 1L));
        assertEquals(1L, deletedBoardId);
    }

    @Test
    @DisplayName("글 삭제 성공 Test 3 - 파일이 있는 경우")
    void deleteSuccess3() {
        Board board3 = Board.builder().id(3L).user(user1).title("제목").content("내용").category(Category.FREE).image("test.txt").build();
        when(boardRepository.findById(3L)).thenReturn(Optional.of(board3));
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));

        Long deletedBoardId = assertDoesNotThrow(() -> boardService.delete("user1", 3L));
        assertEquals(3L, deletedBoardId);
    }

    @Test
    @DisplayName("글 삭제 실패 Test 1 - 유저가 없는 경우")
    void deleteFail1() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> boardService.delete("user1", 1L));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("글 삭제 실패 Test 2 - 삭제하려는 글이 없는 경우")
    void deleteFail2() {
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));

        AppException e = assertThrows(AppException.class, () -> boardService.delete("user1", 1L));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("글 삭제 실패 Test 3 - 작성자와 삭제하려는 유저가 다르고, ADMIN도 아닌 경우")
    void deleteFail3() {
        User user2 = User.builder().id(2L).userName("user2").nickname("nick2").role(UserRole.USER).build();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(user2));

        AppException e = assertThrows(AppException.class, () -> boardService.delete("user2", 1L));
        assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

    @Test
    @DisplayName("Find Category 성공 Test 1 - boardId를 넘겨줬을때 해당 board의 카테고리가 맞는지")
    void findCategorySuccess1() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board1));

        Category category = assertDoesNotThrow(() -> boardService.findCategory(1L));
        assertEquals(Category.FREE, category);
    }

    @Test
    @DisplayName("Find Category 실패 Test 1 - boardId에 해당하는 board가 없는 경우")
    void findCategoryFail1() {
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> boardService.findCategory(1L));
        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("리스트 조회 성공 Test 1 - 검색하지 않은 경우")
    void listSuccess1() {

        Board board3 = Board.builder().id(3L).user(user1).title("제목2").category(Category.FREE).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board1, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board1);
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findAllByCategory(Category.FREE, pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.boardList(pageable, null, null, Category.FREE));
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board3.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("리스트 조회 성공 Test 2 - 제목으로 검색한 경우")
    void listSuccess2() {

        Board board3 = Board.builder().id(3L).user(user1).title("제목2").category(Category.FREE).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board1, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board1);
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndTitleContains(Category.FREE, "제목", pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.boardList(pageable, "TITLE", "제목", Category.FREE));
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board3.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("리스트 조회 성공 Test 3 - 작성자로 검색한 경우")
    void listSuccess3() {

        Board board3 = Board.builder().id(3L).user(user1).title("제목2").category(Category.FREE).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board1, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board1);
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndUserNicknameContains(Category.FREE, user1.getNickname(), pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.boardList(pageable, "NICKNAME", user1.getNickname(), Category.FREE));
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board3.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 1 - 검색하지 않은 경우")
    void portfolioListSuccess1() {

        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region1).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);
        boardList.add(board4);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findAllByCategory(Category.PORTFOLIO, pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, null, null, null, null));
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board4.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 2 - 제목 검색 + 지역 검색 X")
    void portfolioListSuccess2() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);
        boardList.add(board4);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndTitleContains(Category.PORTFOLIO, "제목", pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "TITLE", "제목", null, 998L));
        assertEquals(2, result.getTotalElements());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board4.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 3 - 제목 검색 + 지역 1로만 검색")
    void portfolioListSuccess3() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndTitleContainsAndRegionRegion1(Category.PORTFOLIO, "제목", "경기도", pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "TITLE", "제목", "경기도", 999L));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 4 - 제목 검색 + 지역 1, 2만 검색")
    void portfolioListSuccess4() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndTitleContainsAndRegionId(Category.PORTFOLIO, "제목", 30L, pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "TITLE", "제목", "경기도", 30L));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 5 - 닉네임 검색 + 지역 검색 X")
    void portfolioListSuccess5() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);
        boardList.add(board4);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndUserNicknameContains(Category.PORTFOLIO, user1.getNickname(), pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "NICKNAME", user1.getNickname(), null, 998L));
        assertEquals(2, result.getTotalElements());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(board4.getTitle(), result.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 6 - 닉네임 검색 + 지역 1로만 검색")
    void portfolioListSuccess6() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndUserNicknameContainsAndRegionRegion1(Category.PORTFOLIO, user1.getNickname(), "경기도", pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "NICKNAME", user1.getNickname(), "경기도", 999L));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("포트폴리오 리스트 조회 성공 Test 7 - 닉네임 검색 + 지역 1, 2만 검색")
    void portfolioListSuccess7() {

        Region region2 = Region.builder().id(128L).region1("서울특별시").region2("강남구").build();
        Board board3 = Board.builder().id(3L).user(user1).title("제목3").category(Category.PORTFOLIO).region(region1).build();
        Board board4 = Board.builder().id(4L).user(user1).title("제목4").category(Category.PORTFOLIO).region(region2).build();

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(board3, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(board4, BaseEntity.class, "createdAt", now, LocalDateTime.class);

        List<Board> boardList = new ArrayList<>();
        boardList.add(board3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boardPage = new PageImpl<>(boardList, pageable, 1);

        when(boardRepository.findByCategoryAndUserNicknameContainsAndRegionId(Category.PORTFOLIO, user1.getNickname(), 30L, pageable)).thenReturn(boardPage);

        Page<BoardListResponse> result = assertDoesNotThrow(() -> boardService.portfolioList(pageable, "NICKNAME", user1.getNickname(), "경기도", 30L));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(board3.getTitle(), result.getContent().get(0).getTitle());
    }
}