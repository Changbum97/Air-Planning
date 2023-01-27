package com.example.airplanning.service;

import com.example.airplanning.domain.dto.UserDto;
import com.example.airplanning.domain.dto.UserJoinRequest;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserDto findUser(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return UserDto.of(user);
    }

    public UserDto join(UserJoinRequest request) {

        // userName, nickname 중복 체크
        if(userRepository.existsByUserName(request.getUserName()) ||
                userRepository.existsByNickname(request.getNickname())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String encodedPassword = encoder.encode(request.getPassword());
        User user = userRepository.save(request.toEntity(encodedPassword));
        return UserDto.of(user);
    }

    public void checkPassword(String userName, String password) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if(!encoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

    }

    @Transactional
    public void editUserInfo(String password, String nickname, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String changedPassword = user.getPassword();
        String changedNickname = user.getNickname();
        //String changedImage = user.getImage();

        if (!password.equals("")) {
            changedPassword = encoder.encode(password);
        }

        if (!nickname.equals("")) {
            changedNickname = nickname;
        }

        /*if (!image.equals("")) {
            changedImage = image;
        }*/

        user.updateUser(changedPassword, changedNickname);
        userRepository.save(user);

    }

    public boolean checkUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }

    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}
