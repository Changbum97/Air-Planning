package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.dto.user.UserJoinRequest;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static  org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final AmazonS3 amazonS3 = mock(AmazonS3.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    UserService userService;

    @BeforeEach
    void beforeEach() {
        userService = new UserService(amazonS3, userRepository, encoder);
    }

    @Test
    @DisplayName("userName으로 유저 찾기 실패 - 유저 없음")
    void findUser_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.findUser("user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("userName으로 유저 찾기 성공")
    void findUser_success() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(spy(User.builder().userName("user").role(UserRole.USER).build())));
        // then
        UserDto userDto = userService.findUser("user");
        assertThat(userDto.getUserName(), is("user"));
    }

    @Test
    @DisplayName("Nickname으로 유저 찾기 실패 - 유저 없음")
    void findNickname_fail() {
        // when
        when(userRepository.findByNickname("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.findNickname("user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("Nickname으로 유저 찾기 성공")
    void findNickname_success() {
        // when
        when(userRepository.findByNickname("user")).thenReturn(Optional.of(spy(User.builder().userName("user").role(UserRole.USER).build())));
        // then
        UserDto userDto = userService.findNickname("user");
        assertThat(userDto.getUserName(), is("user"));
    }

    @Test
    @DisplayName("유저 프로필 찾기 실패 - 유저 없음")
    void userProfile_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.userProfile("user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("유저 프로필 찾기 성공")
    void userProfile_success() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(spy(User.builder().image("testImage").build())));
        // then
        String result = userService.userProfile("user");
        assertThat(result, is("testImage"));
    }

    @Test
    @DisplayName("회원 가입 실패 - userName 중복")
    void join_fail1() {
        // given
        UserJoinRequest request = new UserJoinRequest("nickname", "email", "username", "pwd", "code");
        // when
        when(userRepository.existsByUserName("username")).thenReturn(true);
        when(userRepository.existsByNickname("nickname")).thenReturn(false);
        // then
        AppException error = assertThrows(AppException.class, () -> userService.join(request));
        assertThat(error.getErrorCode(), is(ErrorCode.INVALID_REQUEST));
    }

    @Test
    @DisplayName("회원 가입 실패 - nickname 중복")
    void join_fail2() {
        // given
        UserJoinRequest request = new UserJoinRequest("nickname", "email", "username", "pwd", "code");
        // when
        when(userRepository.existsByUserName("username")).thenReturn(false);
        when(userRepository.existsByNickname("nickname")).thenReturn(true);
        // then
        AppException error = assertThrows(AppException.class, () -> userService.join(request));
        assertThat(error.getErrorCode(), is(ErrorCode.INVALID_REQUEST));
    }

    @Test
    @DisplayName("회원 가입 성공")
    void join_success() {
        // given
        UserJoinRequest request = new UserJoinRequest("nickname", "email", "username", "pwd", "code");
        // when
        when(userRepository.existsByUserName("username")).thenReturn(false);
        when(userRepository.existsByNickname("nickname")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(spy(User.builder().userName("success").role(UserRole.USER).build()));
        // then
        UserDto userDto = userService.join(request);
        assertThat(userDto.getUserName(), is("success"));
    }

    @Test
    @DisplayName("비밀번호 확인 실패 - 유저 없음")
    void checkPassword_fail1() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.checkPassword("user", "pwd"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("비밀번호 확인 실패 - 비밀번호 다름")
    void checkPassword_fail2() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("test1").build()));
        when(encoder.matches("test2", "test1")).thenReturn(false);
        // then
        AppException error = assertThrows(AppException.class, () -> userService.checkPassword("user", "pwd"));
        assertThat(error.getErrorCode(), is(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void checkPassword_success() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("test1").build()));
        when(encoder.matches("test2", "test1")).thenReturn(true);
        // then
        userService.checkPassword("user", "test2");
        verify(encoder).matches("test2", "test1");
    }

    @Test
    @DisplayName("유저 정보 수정 실패 - 유저 없음")
    void editUserInfo_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.editUserInfo("pwd", "nickname", null, "user"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("유저 정보 수정 성공1")
    void editUserInfo_success1() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "image2", "jpg", "test file".getBytes(StandardCharsets.UTF_8));
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("pwd1").nickname("nickname1").image("https://air-planning.s3.ap-northeast-2.amazonaws.com/default.jpeg").build()));
        // then
        userService.editUserInfo("pwd2", "nickname2", file, "user");
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("유저 정보 수정 성공2")
    void editUserInfo_success2() throws IOException {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("pwd1").nickname("nickname1").image("image1").build()));
        // then
        userService.editUserInfo("", "", null, "user");
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("파일 업로드 실패 - amazonS3 예외 발생")
    void uploadFile_fail() {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "image2", "jpg", "test file".getBytes(StandardCharsets.UTF_8));
        // when
        when(amazonS3.putObject(any(), any(), any(), any())).thenThrow(AppException.class);
        // then
        AppException error = assertThrows(AppException.class, () -> userService.uploadFile(file, "test"));
        assertThat(error.getErrorCode(), is(ErrorCode.FILE_UPLOAD_ERROR));
    }

    @Test
    @DisplayName("파일 업로드 성공1")
    void uploadFile_success1() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "image2", "jpg", "test file".getBytes(StandardCharsets.UTF_8));
        // then
        String result = userService.uploadFile(file, null);
        assertThat(result.charAt(0), is('h'));
    }

    @Test
    @DisplayName("파일 업로드 성공2")
    void uploadFile_success2() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "image2", "jpg", "test file".getBytes(StandardCharsets.UTF_8));
        // then
        String result = userService.uploadFile(file, "test");
        assertThat(result.charAt(0), is('h'));
    }

    @Test
    @DisplayName("ID로 유저 찾기 실패 - 유저 없음")
    void findUserById_fail() {
        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.findUserById(1L));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("ID로 유저 찾기 성공")
    void findUserById_success() {
        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(spy(User.builder().userName("user").role(UserRole.USER).build())));
        // then
        UserDto userDto = userService.findUserById(1L);
        assertThat(userDto.getUserName(), is("user"));
    }

    @Test
    @DisplayName("username 체크")
    void checkUserName() {
        // when
        when(userRepository.existsByUserName("username")).thenReturn(true);
        // then
        boolean result = userService.checkUserName("username");
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("nickname 체크")
    void checkNickname() {
        // when
        when(userRepository.existsByNickname("nickname")).thenReturn(true);
        // then
        boolean result = userService.checkNickname("nickname");
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("email 체크")
    void checkEmail() {
        // when
        when(userRepository.existsByEmail("email")).thenReturn(true);
        // then
        boolean result = userService.checkEmail("email");
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Email로 유저 찾기 실패 - 유저 없음")
    void findIdByEmail_fail() {
        // when
        when(userRepository.findByEmail("email")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.findIdByEmail("email"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("Email로 유저 찾기 성공")
    void findIdByEmail_success() {
        // when
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(spy(User.builder().userName("user").role(UserRole.USER).build())));
        // then
        String result = userService.findIdByEmail("email");
        assertThat(result, is("user"));
    }

    @Test
    @DisplayName("아이디 + 이메일로 비밀번호 찾기")
    void findPassword() {
        // when
        when(userRepository.existsByUserNameAndEmail("username", "email")).thenReturn(true);
        // then
        boolean result = userService.findPassword("username", "email");
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유저 없음")
    void changePassword_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.changePassword("user", "pwd"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("password").build()));
        // then
        userService.changePassword("user", "pwd");
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("닉네임 등록 실패 - 유저 없음")
    void setNickname_fail() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        // then
        AppException error = assertThrows(AppException.class, () -> userService.setNickname("user", "pwd"));
        assertThat(error.getErrorCode(), is(ErrorCode.USER_NOT_FOUNDED));
    }

    @Test
    @DisplayName("닉네임 등록 성공")
    void setNickname_success() {
        // when
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(User.builder().password("password").build()));
        // then
        userService.setNickname("user", "newNickname");
        verify(userRepository).save(any());
    }

}