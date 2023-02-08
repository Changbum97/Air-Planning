package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.airplanning.domain.dto.board.BoardCreateRequest;
import com.example.airplanning.domain.dto.BoardDto;
import com.example.airplanning.domain.dto.board.BoardModifyRequest;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    @Transactional
    public BoardDto write(BoardCreateRequest boardCreateRequest, String username) {

        User user = userRepository.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("게시글 작성 권한이 없습니다."));
        User userEntity = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED, String.format("%s not founded", username)));
        Board savedBoardEntity = boardRepository.save(boardCreateRequest.toEntity(userEntity));

        BoardDto boardDto = BoardDto.builder()
                .id(savedBoardEntity.getId())
                .build();

        return boardDto;
    }
    
    public BoardDto detail(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }


    // 수정
    public BoardDto modify(BoardModifyRequest modifyRequest, String userName, Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        board.modify(modifyRequest.getTitle(), modifyRequest.getContent());
        boardRepository.save(board);
        return BoardDto.of(board);

    }

    public Board view(Long id){
        return boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
    }
    
    public BoardDto rankUpWrite(BoardCreateRequest boardCreateRequest, String userName) {
        User userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Board board= boardRepository.save(boardCreateRequest.toEntity(userEntity));

        return BoardDto.of(board);
    }


    // 플래너신청조회
    public BoardDto rankUpDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }
    
    // 삭제
    @Transactional
    public Long delete(String userName, Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        if (!Objects.equals(board.getUser().getUserName(),userName)){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        boardRepository.deleteById(id);
        return id;


    }

    //포토폴리오 작성
    @Transactional
    public void writePortfolio(BoardCreateRequest req, MultipartFile file, String username) throws IOException {

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String changedFile = null;

        if (file != null) {
            changedFile = uploadFile(file);
        }

        Board board = Board.builder()
                .user(user)
                .category(Category.PORTFOLIO)
                .title(req.getTitle())
                .content(req.getContent())
                .image(changedFile)
                .build();

        boardRepository.save(board);

    }

    // 포토폴리오 상세 조회
    public BoardDto portfolioDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return BoardDto.of(board);
    }

    //기존 이미지 삭제
    public void deleteFile(String filePath) {
        //앞의 defaultUrl을 제외한 파일이름만 추출
        String[] bits = filePath.split("/");
        String fileName = bits[bits.length-1];
        //S3에서 delete
        amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }

    //파일 업로드
    public String uploadFile(MultipartFile file) throws IOException {

        String defaultUrl = "https://airplanning-bucket.s3.ap-northeast-2.amazonaws.com/";
        String fileName = generateFileName(file);

        try {
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), getObjectMetadata(file));
        } catch (AppException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        
        return defaultUrl + fileName;

    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        //objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }

    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
    }

}