package com.example.airplanning.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.dto.user.UserJoinRequest;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AmazonS3 amazonS3;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public UserDto findUser(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return UserDto.of(user);
    }

    public UserDto findNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
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
    public void editUserInfo(String password, String nickname, MultipartFile file, String userName) throws IOException {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String changedPassword = user.getPassword();
        String changedNickname = user.getNickname();
        String changedImage = user.getImage();

        if (!password.equals("")) {
            changedPassword = encoder.encode(password);
        }

        if (!nickname.equals("")) {
            changedNickname = nickname;
        }

        if (file != null) {
            changedImage = uploadFile(file, user.getImage());
        }

        user.updateUser(changedPassword, changedNickname, changedImage);
        userRepository.save(user);

    }

    //기존 이미지 삭제
    public void deleteFile(String filePath) {
        //앞의 defaultUrl을 제외한 파일이름만 추출
        String[] bits = filePath.split("/");
        String fileName = bits[bits.length-1];
        //S3에서 delete
        amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }

    //이미지 변경
    public String uploadFile(MultipartFile file, String filePath) throws IOException {

        String defaultUrl = "https://airplanning-bucket.s3.ap-northeast-2.amazonaws.com/";
        String fileName = generateFileName(file);

        try {
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), getObjectMetadata(file));
        } catch (AppException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        deleteFile(filePath);
        return defaultUrl + fileName;

    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }

    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
    }

    public UserDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return UserDto.of(user);
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

    // 이메일로 아이디 찾기
    public String findIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUNDED));
        return user.getUserName();
    }

    // 아이디 + 이메일로 비밀번호 찾기
    public boolean findPassword(String userName, String email) {
        return userRepository.existsByUserNameAndEmail(userName, email);
    }

    // 비밀번호 변경
    public void changePassword(String userName, String newPassword) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String encodedPassword = encoder.encode(newPassword);
        user.changePassword(encodedPassword);
        userRepository.save(user);
    }

    // 닉네임 등록
    public void setNickname(String userName, String newNickname) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        user.setNickname(newNickname);
        userRepository.save(user);
    }
}
