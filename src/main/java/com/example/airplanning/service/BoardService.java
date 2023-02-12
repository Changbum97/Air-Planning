package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

//    private final LikeRepository likeRepository;
//    private final AlarmService alarmRepository;


    @Transactional
    public BoardDto write(BoardCreateRequest boardCreateRequest, String userName) {


        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED, String.format("%s not founded", userName)));
        Board savedBoardEntity = boardRepository.save(boardCreateRequest.toEntity(user));

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

    @Transactional
    public void rankUpWrite(BoardCreateRequest boardCreateRequest, String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Board board = Board.builder()
                .user(user)
                .category(Category.RANK_UP)
                .title(boardCreateRequest.getTitle())
                .content(boardCreateRequest.getContent())
                .build();

        boardRepository.save(board);
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

        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        if (board.getImage() != null) {
            deleteFile(board.getImage());
        }

        boardRepository.deleteById(id);
        return id;

    }

    public Page<BoardListResponse> boardList(Pageable pageable, String searchType, String keyword){
        Page<Board> board;

        if(searchType == null) {
            board = boardRepository.findAllByCategory(Category.FREE, pageable);
        } else {
            // 글 제목으로 검색
            if (searchType.equals("TITLE")) {
                board = boardRepository.findByCategoryAndTitleContains(Category.FREE, keyword, pageable);
            }
            // 작성자 닉네임으로 검색
            else {
                board = boardRepository.findByCategoryAndUserNicknameContains(Category.FREE, keyword, pageable);
            }
        }
        return BoardListResponse.toDtoList(board);
    }

//    @Transactional
//    public void like(String userName, Long id) {
//
//        Board board = boardRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
//
//        User user = userRepository.findByUserName(userName)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
//
//        // 좋아요 중복체크
//        likeRepository.findByUserBoard(user, board)
//                .ifPresent(item -> {
//                    throw new AppException(ErrorCode.ALREADY_LIKED)});
//
//        likeRepository.save(Like.of(user, board));
//        alarmRepository.save(Alarm.of(board.getUser(), AlarmType.NEW_LIKE_ON_POST,
//                user.getId(), board.getId()));
//
//    }
//
//    public Integer likeCount(Long id) {
//        // Board 유무 확인
//        Board board = boardRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
//        return likeRepository.countByBoard(id);
//    }

    //포토폴리오 작성
    @Transactional
    public Long writePortfolio(BoardCreateRequest req, MultipartFile file, String username) throws IOException {

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

        return board.getId();

    }

    //포토폴리오 수정
    @Transactional
    public void portfolioModify(PortfolioModifyRequest req, MultipartFile file, String username, Long boardId) throws IOException {

        //AccessDeniedHandler에서 막혔을 듯
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        //혹시 모를 버튼이 아닌 url 접근을 막기 위해
        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        String changedFile = null;

        //만약 기존 게시글에 파일이 있던 경우
        if (board.getImage() !=  null) {
            if (req.getImage().equals("changed")) { //파일 변경시
                if (file != null) { // 파일을 다른 파일로 교체한 경우
                    changedFile = uploadFile(file);
                    deleteFile(board.getImage()); //기존 파일 삭제
                } else { //파일 삭제한 경우
                    deleteFile(board.getImage()); //기존 파일 삭제
                }
            } else { //파일 변경이 없던 경우
                changedFile = board.getImage();
            }
        } else { //기존 파일이 없던 경우
            if (file != null) { //새 파일 업로드
                changedFile = uploadFile(file);
            }
        }

        board.modify(req.getTitle(), req.getContent(), changedFile);
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

    public Board update(Long boardId){
        return boardRepository.findById(boardId).
                orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
    }

    public BoardDto rankUpdate(BoardModifyRequest modifyRequest, String userName, Long boardId) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        board.modify(modifyRequest.getTitle(), modifyRequest.getContent());
        boardRepository.save(board);
        return BoardDto.of(board);

    }

    @Transactional
    public Long rankDelete(Long id, String userName){
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));
        Board board = boardRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (board.getUser().getUserName() != user.getUserName()){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        boardRepository.deleteById(id);
        return id;
    }

}