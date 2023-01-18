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
        User user = userRepository.save(request.toEntity(encoder.encode(request.getPassword())));
        return UserDto.of(user);
    }

    public boolean checkUserName(String userName) {
        boolean validation = false;

        if (userRepository.findByUserName(userName).isEmpty()) validation = true;
        else validation = false;

        return validation;
    }

    public boolean checkEmail(String email) {
        boolean validation = false;

        if (userRepository.findByEmail(email).isEmpty()) validation = true;
        else validation = false;

        return validation;
    }

    public boolean checkPhoneNumber(String phoneNumber) {
        boolean validation = false;

        if (userRepository.findByPhoneNumber(phoneNumber).isEmpty()) validation = true;
        else validation = false;

        return validation;
    }
}
