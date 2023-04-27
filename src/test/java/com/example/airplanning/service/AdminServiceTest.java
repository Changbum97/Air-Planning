package com.example.airplanning.service;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.board.RankUpRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.PlannerRepository;
import com.example.airplanning.repository.RegionRepository;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final SessionRegistry sessionRegistry= mock(SessionRegistry.class);
    private final PlannerRepository plannerRepository= mock(PlannerRepository.class);
    private final RegionRepository regionRepository= mock(RegionRepository.class);
    private final BoardRepository boardRepository= mock(BoardRepository.class);
    private final AlarmService alarmService= mock(AlarmService.class);

    AdminService adminService;

    @BeforeEach
    void beforeEach() {
        adminService = new AdminService(userRepository, sessionRegistry, plannerRepository, regionRepository, alarmService, boardRepository);
    }

    @Test
    @DisplayName("플래너 등업 신청 수락 실패 - 유저 없음")
    void rankUpRequestAccept_fail1() {
        // given
        RankUpRequest request = new RankUpRequest("requestUser", "rankUpPlease", "region1 region2", 1L, 1000);

        // when
        when(userRepository.findByUserName("requestUser")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> adminService.changeRankToPlanner(request));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("플래너 등업 신청 수락 실패 - 지역 없음")
    void rankUpRequestAccept_fail2() {
        // given
        RankUpRequest request = new RankUpRequest("requestUser", "rankUpPlease", "region1 region2", 1L, 1000);
        User foundUser = User.builder().build();

        // when
        when(userRepository.findByUserName("requestUser")).thenReturn(Optional.of(foundUser));
        when(regionRepository.findByRegion1AndRegion2("region1", "region2")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> adminService.changeRankToPlanner(request));
        assertThat(error.getErrorCode(), is(ErrorCode.REGION_NOT_FOUND));
    }

    @Test
    @DisplayName("플래너 등업 신청 수락 실패 - 신청글 없음")
    void rankUpRequestAccept_fail3() {
        // given
        RankUpRequest request = new RankUpRequest("requestUser", "rankUpPlease", "region1 region2", 1L, 1000);
        User foundUser = User.builder().build();
        Region selectedRegion = new Region(1L, "region1", "region2");

        // when
        when(userRepository.findByUserName("requestUser")).thenReturn(Optional.of(foundUser));
        when(regionRepository.findByRegion1AndRegion2("region1", "region2")).thenReturn(Optional.of(selectedRegion));
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> adminService.changeRankToPlanner(request));
        assertThat(error.getErrorCode(), is(ErrorCode.BOARD_NOT_FOUND));
    }

    @Test
    @DisplayName("플래너 등업 신청 수락 성공")
    void rankUpRequestAccept_success() {
        // given
        RankUpRequest request = new RankUpRequest("requestUser", "rankUpPlease", "region1 region2", 1L, 1000);
        User foundUser = User.builder().build();
        User changedUser = spy(User.builder().role(UserRole.PLANNER).build());
        Region selectedRegion = new Region(1L, "region1", "region2");
        Board requestBoard = new Board().builder().build();

        // when
        when(userRepository.findByUserName("requestUser")).thenReturn(Optional.of(foundUser));
        when(userRepository.save(any())).thenReturn(changedUser);
        when(regionRepository.findByRegion1AndRegion2("region1", "region2")).thenReturn(Optional.of(selectedRegion));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(requestBoard));

        // then
        UserDto result = adminService.changeRankToPlanner(request);
        assertThat(result.getRole(), is("PLANNER"));
    }

    @Test
    @DisplayName("유저 등급 변경 실패 - 유저 없음")
    void changeRank_fail() {
        // when
        when(userRepository.findByNickname("nick")).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> adminService.changeRank("nick", "BLACKLIST", 1L));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("유저 등급 변경 실패 - 신청글 없음")
    void changeRank_fail2() {
        // given
        User foundUser = spy(User.class);
        User changedUser = spy(User.builder().role(UserRole.BLACKLIST).build());

        // when
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(foundUser));
        when(userRepository.save(any())).thenReturn(changedUser);
        when(plannerRepository.findByUser(changedUser)).thenReturn(Optional.empty());
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        AppException error = assertThrows(AppException.class, () -> adminService.changeRank("nick", "BLACKLIST", 1L));
        assertThat(error.getErrorCode(), is(ErrorCode.BOARD_NOT_FOUND));
    }

    @Test
    @DisplayName("유저 등급 변경 성공")
    void changeRank_success1() {
        // given
        User foundUser = spy(User.builder().id(1L).build());
        Planner foundPlanner = spy(Planner.class);
        User changedUser = spy(User.builder().role(UserRole.USER).planner(foundPlanner).build());
        Board foundBoard = spy(Board.class);

        // when
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(foundUser));
        when(userRepository.save(any())).thenReturn(changedUser);
        when(plannerRepository.findByUser(changedUser)).thenReturn(Optional.of(foundPlanner));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(foundBoard));

        // then
        UserDto result = adminService.changeRank("nick", "User", 1L);
        assertThat(result.getRole(), is("USER"));
    }

    @Test
    @DisplayName("유저 등급 변경 성공 - 블랙리스트 처리")
    void changeRank_success2() {
        // given
        User foundUser = spy(User.builder().id(1L).build());
        Planner foundPlanner = spy(Planner.class);
        User changedUser = spy(User.builder().role(UserRole.BLACKLIST).planner(foundPlanner).build());
        Board foundBoard = spy(Board.class);

        UserDetail userDetail1 = UserDetail.builder().id(1L).build();
        UserDetail userDetail2 = UserDetail.builder().id(2L).build();

        List<Object> userDetails = new ArrayList<>();
        userDetails.add(userDetail1);
        userDetails.add(userDetail2);

        List<SessionInformation> sessionList = new ArrayList<>();
        sessionList.add(new SessionInformation(userDetail1, "1", new Date()));

        // when
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(foundUser));
        when(userRepository.save(any())).thenReturn(changedUser);
        when(plannerRepository.findByUser(changedUser)).thenReturn(Optional.of(foundPlanner));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(foundBoard));
        when(sessionRegistry.getAllPrincipals()).thenReturn(userDetails);
        when(sessionRegistry.getAllSessions(userDetail1, false)).thenReturn(sessionList);

        // then
        UserDto result = adminService.changeRank("nick", "BLACKLIST", 1L);
        assertThat(result.getRole(), is("BLACKLIST"));
    }

}