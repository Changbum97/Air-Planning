package com.example.airplanning.controller.api;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.board.BoardDto;
import com.example.airplanning.domain.dto.board.BoardListResponse;
import com.example.airplanning.domain.dto.board.BoardUpdateRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.RegionRepository;
import com.example.airplanning.service.BoardService;
import com.example.airplanning.service.LikeService;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardRestController.class)
class BoardRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BoardService boardService;

    @MockBean
    LikeService likeService;

    @MockBean
    UserService userService;

    @MockBean
    RegionRepository regionRepository;

    @Autowired
    ObjectMapper objectMapper;

    private static UserDto user1Dto, adminDto;
    private static UserDetail user1Detail, adminDetail;
    private static BoardDto board1Dto;
    private static BoardCreateRequest boardCreateRequest;

    @BeforeEach
    void setUp() {
        user1Dto = UserDto.builder().id(1L).userName("user1").nickname("nick1").password("1234").role("USER").build();
        adminDto = UserDto.builder().id(2L).userName("admin").nickname("관리자").password("1234").role("ADMIN").build();
        user1Detail = UserDetail.builder().id(1L).userName("user1").password("1234").role("USER").build();
        adminDetail = UserDetail.builder().id(2L).userName("admin").password("1234").role("ADMIN").build();
        board1Dto = BoardDto.builder().id(1L).userName("user1").nickname("nick1").title("제목1").content("내용1").likeCnt(0).views(0).category(Category.FREE).build();
        boardCreateRequest = BoardCreateRequest.builder().title("제목1").content("내용1").build();
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 성공 Test 1 - 자유게시판")
    void writeSuccess1() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any())).thenReturn(board1Dto);

        mockMvc.perform(multipart("/api/boards/free")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.title").value("제목1"))
                .andExpect(jsonPath("$.result.userName").value("user1"))
                .andExpect(jsonPath("$.result.category").value("FREE"))
                .andDo(print());

        verify(boardService).writeWithFile(any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 성공 Test 2 - 등업게시판")
    void writeSuccess2() throws Exception {

        boardCreateRequest.setRegionId(30L);
        boardCreateRequest.setAmount(500);
        Region region = Region.builder().id(30L).region1("경기도").region2("수원시").build();
        board1Dto.setRegion(region.getRegion1() + " " + region.getRegion2());
        board1Dto.setAmount(500);
        board1Dto.setCategory(Category.RANK_UP);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any())).thenReturn(board1Dto);
        when(regionRepository.findById(any())).thenReturn(Optional.of(region));

        mockMvc.perform(multipart("/api/boards/rankup")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.title").value("제목1"))
                .andExpect(jsonPath("$.result.region").value("경기도 수원시"))
                .andExpect(jsonPath("$.result.amount").value(500))
                .andExpect(jsonPath("$.result.category").value("RANK_UP"))
                .andDo(print());

        verify(boardService).writeWithFile(any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 성공 Test 3 - 포트폴리오 게시판")
    void writeSuccess3() throws Exception {

        board1Dto.setCategory(Category.PORTFOLIO);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any())).thenReturn(board1Dto);

        mockMvc.perform(multipart("/api/boards/portfolio")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.title").value("제목1"))
                .andExpect(jsonPath("$.result.userName").value("user1"))
                .andExpect(jsonPath("$.result.category").value("PORTFOLIO"))
                .andDo(print());

        verify(boardService).writeWithFile(any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 성공 Test 4 - 신고게시판")
    void writeSuccess4() throws Exception {

        boardCreateRequest.setTitle("user1");
        board1Dto.setTitle("user1");
        board1Dto.setCategory(Category.REPORT);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any())).thenReturn(board1Dto);

        mockMvc.perform(multipart("/api/boards/report")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.title").value("user1"))
                .andExpect(jsonPath("$.result.userName").value("user1"))
                .andExpect(jsonPath("$.result.category").value("REPORT"))
                .andDo(print());

        verify(boardService).writeWithFile(any(), any(), any(), any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("글 작성 실패 Test 1 - 로그인하지 않은 유저가 작성한 경우")
    void writeFail1() throws Exception {

        mockMvc.perform(multipart("/api/boards/free")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 실패 Test 2 - 파일 업로드 에러")
    void writeFail2() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any()))
                .thenThrow(new AppException(ErrorCode.FILE_UPLOAD_ERROR));

        mockMvc.perform(multipart("/api/boards/free")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("파일 업로드 과정 중 오류가 발생했습니다. 다시 시도해 주세요."))
                .andDo(print());

        verify(boardService).writeWithFile(any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 실패 Test 3 - 파일 업로드 에러가 아닌 다른 AppException")
    void writeFail3() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any()))
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUNDED));

        mockMvc.perform(multipart("/api/boards/free")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 실패 Test 4 - AppException이 아닌 다른 Exception")
    void writeFail4() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(multipart("/api/boards/free")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("에러 발생"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 작성 실패 Test 5 - 카테고리가 잘못된 경우")
    void writeFail5() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.writeWithFile(any(), any(), any(), any())).thenReturn(board1Dto);

        mockMvc.perform(multipart("/api/boards/category")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardCreateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 성공 Test 1")
    void detailSuccess1() throws Exception {

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.detail(1L, false, Category.FREE)).thenReturn(board1Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(boardService).detail(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 성공 Test 2 - REPORT를 작성자가 조회한 경우")
    void detailSuccess2() throws Exception {

        board1Dto.setCategory(Category.REPORT);
        when(boardService.findCategory(1L)).thenReturn(Category.REPORT);
        when(boardService.detail(1L, false, Category.REPORT)).thenReturn(board1Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(boardService).detail(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 성공 Test 3 - REPORT를 관리자가 조회한 경우")
    void detailSuccess3() throws Exception {

        board1Dto.setCategory(Category.REPORT);
        when(boardService.findCategory(1L)).thenReturn(Category.REPORT);
        when(boardService.detail(1L, false, Category.REPORT)).thenReturn(board1Dto);
        when(userService.findUser("admin")).thenReturn(adminDto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(adminDetail)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(boardService).detail(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 성공 Test 4 - RANKUP을 작성자가 조회한 경우")
    void detailSuccess5() throws Exception {

        board1Dto.setCategory(Category.RANK_UP);
        when(boardService.findCategory(1L)).thenReturn(Category.RANK_UP);
        when(boardService.detail(1L, false, Category.RANK_UP)).thenReturn(board1Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(boardService).detail(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 성공 Test 5 - RANKUP을 관리자가 조회한 경우")
    void detailSuccess4() throws Exception {

        board1Dto.setCategory(Category.RANK_UP);
        when(boardService.findCategory(1L)).thenReturn(Category.RANK_UP);
        when(boardService.detail(1L, false, Category.RANK_UP)).thenReturn(board1Dto);
        when(userService.findUser("admin")).thenReturn(adminDto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(adminDetail)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(boardService).detail(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 실패 Test 1 - REPORT를 작성자, 관리자가 아닌 유저가 조회한 경우")
    void detailFail1() throws Exception {

        board1Dto.setCategory(Category.REPORT);
        board1Dto.setUserName("user2");

        when(boardService.findCategory(1L)).thenReturn(Category.REPORT);
        when(boardService.detail(1L, false, Category.REPORT)).thenReturn(board1Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 조회 실패 Test 2 - RANKUP을 작성자, 관리자가 아닌 유저가 조회한 경우")
    void detailFail2() throws Exception {

        board1Dto.setCategory(Category.RANK_UP);
        board1Dto.setUserName("user2");

        when(boardService.findCategory(1L)).thenReturn(Category.RANK_UP);
        when(boardService.detail(1L, false, Category.RANK_UP)).thenReturn(board1Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(get("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 성공 Test 1")
    void updateSuccess1() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);
        BoardDto board2Dto = BoardDto.builder().id(1L).userName("user1").nickname("nick1").title("수정된 제목")
                                        .content("수정된 내용").likeCnt(0).views(0).category(Category.FREE).build();

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenReturn(board2Dto);
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.title").value("수정된 제목"))
                .andExpect(jsonPath("$.result.content").value("수정된 내용"))
                .andDo(print());

        verify(boardService).modify(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 실패 Test 1 - 파일 업로드 에러")
    void updateFail1() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenThrow(new AppException(ErrorCode.FILE_UPLOAD_ERROR));
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("파일 업로드 과정 중 오류가 발생했습니다. 다시 시도해 주세요."))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 실패 Test 2 - 게시글이 존재하지 않는 경우")
    void updateFail2() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenThrow(new AppException(ErrorCode.BOARD_NOT_FOUND));
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("게시글이 존재하지 않습니다."))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 실패 Test 3 - 권한이 없는 경우")
    void updateFail3() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);
        board1Dto.setUserName("user2");

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("작성자만 수정이 가능합니다."))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 실패 Test 4 - 다른 AppException")
    void updateFail4() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenThrow(new AppException(ErrorCode.USER_NOT_FOUNDED));
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 실패 Test 5 - AppException이 아닌 다른 Exception")
    void updateFail5() throws Exception {

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest("수정된 제목", "수정된 내용", null);

        when(boardService.findCategory(1L)).thenReturn(Category.FREE);
        when(boardService.modify(any(), any(), any(), any(), any())).thenThrow(new RuntimeException());
        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/boards/1")
                        .part(new MockPart("request", objectMapper.writeValueAsBytes(boardUpdateRequest)))
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("에러 발생"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("글 삭제 성공 Test 1")
    void deleteSuccess1() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);

        mockMvc.perform(delete("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").value("글이 삭제 되었습니다."))
                .andDo(print());

        verify(boardService).delete(any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("글 삭제 실패 Test 1 - 에러가 발생한 경우")
    void deleteFail1() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.delete(any(), any())).thenThrow(new RuntimeException());

        mockMvc.perform(delete("/api/boards/1")
                        .with(csrf())
                        .with(user(user1Detail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").value("글 삭제 중 에러가 발생하였습니다. 다시 시도해주세요."))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 1 - 자유게시판, 검색 X")
    void listSuccess1() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);
        boardListResponses.add(boardListResponse2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.boardList(any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/FREE/list")
                        .param("page", "1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(2))
                .andDo(print());

        verify(boardService).boardList(any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 2 - 자유게시판, 제목 검색")
    void listSuccess2() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.boardList(any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/FREE/list")
                        .param("page", "1")
                        .param("searchType", "제목")
                        .param("keyword", "제목1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(1))
                .andDo(print());

        verify(boardService).boardList(any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 3 - 자유게시판, 작성자 검색")
    void listSuccess3() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);
        boardListResponses.add(boardListResponse2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.boardList(any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/FREE/list")
                        .param("page", "1")
                        .param("searchType", "작성자")
                        .param("keyword", "nick1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(2))
                .andDo(print());

        verify(boardService).boardList(any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 4 - searchType이 잘못 입력된 경우 => 검색 X")
    void listSuccess4() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);
        boardListResponses.add(boardListResponse2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.boardList(any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/FREE/list")
                        .param("page", "1")
                        .param("searchType", "aaa")
                        .param("keyword", "nick1")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(2))
                .andDo(print());

        verify(boardService).boardList(any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 5 - 신고게시판 에서는 검색 X")
    void listSuccess5() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);
        boardListResponses.add(boardListResponse2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.boardList(any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/REPORT/list")
                        .param("page", "1")
                        .param("searchType", "제목")
                        .param("keyword", "제목")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(2))
                .andDo(print());

        verify(boardService).boardList(any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("리스트 조회 성공 Test 6 - 포트폴리오 게시판")
    void listSuccess6() throws Exception {

        String now = LocalDateTime.now().toString();
        BoardListResponse boardListResponse1 = BoardListResponse.builder().id(1L).nickname("nick1").title("제목1").createdAt(now).build();
        BoardListResponse boardListResponse2 = BoardListResponse.builder().id(2L).nickname("nick1").title("제목2").createdAt(now).build();

        List<BoardListResponse> boardListResponses = new ArrayList<>();
        boardListResponses.add(boardListResponse1);
        boardListResponses.add(boardListResponse2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardListResponse> boardListPage = new PageImpl<>(boardListResponses, pageable, 1);

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.portfolioList(any(), any(), any(), any(), any())).thenReturn(boardListPage);

        mockMvc.perform(get("/api/boards/PORTFOLIO/list")
                        .param("page", "1")
                        .param("searchType", "제목")
                        .param("keyword", "제목")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content.size()").value(2))
                .andDo(print());

        verify(boardService).portfolioList(any(), any(), any(), any(), any());

    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 추가 성공 Test 1 - 자유게시판 글에 좋아요를 추가한 경우")
    void likeSuccess1() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.findCategory(any())).thenReturn(Category.FREE);
        when(likeService.changeLike(any(), any(), any())).thenReturn("좋아요가 추가되었습니다.");

        mockMvc.perform(post("/api/boards/1/like")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("좋아요가 추가되었습니다."))
                .andDo(print());

        verify(likeService).changeLike(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 취소 성공 Test 2 - 자유게시판 글에 좋아요를 취소한 경우")
    void likeSuccess2() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.findCategory(any())).thenReturn(Category.FREE);
        when(likeService.changeLike(any(), any(), any())).thenReturn("좋아요가 취소되었습니다.");

        mockMvc.perform(post("/api/boards/1/like")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("좋아요가 취소되었습니다."))
                .andDo(print());

        verify(likeService).changeLike(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요 추가 실패 Test 1 - 자유게시판이 아닌 게시판의 글에 좋아요를 추가한 경우")
    void likeFail1() throws Exception {

        when(userService.findUser("user1")).thenReturn(user1Dto);
        when(boardService.findCategory(any())).thenReturn(Category.RANK_UP);

        mockMvc.perform(post("/api/boards/1/like")
                        .with(csrf())
                        .with(user(user1Detail)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}