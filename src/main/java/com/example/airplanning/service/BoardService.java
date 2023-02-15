package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
import com.example.airplanning.repository.RegionRepository;
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

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final AlarmService alarmService;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

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

    @Transactional
    public BoardDto detail(Long id, Boolean addView) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getCategory().name().equals("FREE")) {
            throw new AppException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(addView) {
            board.addViews();
        }

        return BoardDto.of(board);
    }

    public Board view(Long id){
        return boardRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
    }

    @Transactional
    public void rankUpWrite(RankUpCreateRequest rankUpCreateRequest, MultipartFile file, String userName) throws IOException {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String changedFile = null;

        if (file != null) {
            changedFile = uploadFile(file);
        }

        Board board = Board.builder()
                .user(user)
                .category(Category.RANK_UP)
                .title(rankUpCreateRequest.getTitle())
                .content(rankUpCreateRequest.getContent())
                .regionId(rankUpCreateRequest.getRegionId())
                .image(changedFile)
                .build();

        boardRepository.save(board);

        List<User> admins = userRepository.findAllByRole(UserRole.ADMIN);
        for (User admin : admins) {
            alarmService.send(admin, AlarmType.REQUEST_CHANGE_ROLE_ALARM, "/boards/rankUp/"+board.getId(), board.getTitle());
        }
    }


    // 플래너신청조회
    public RankUpDetailResponse rankUpDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        Region region = regionRepository.findById(board.getRegionId()).get();
        return RankUpDetailResponse.of(board, region);
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

    // 포트폴리오 리스트
    public Page<BoardListResponse> portfolioList(Pageable pageable, String searchType, String keyword){
        Page<Board> board;

        if(searchType == null) {
            board = boardRepository.findAllByCategory(Category.PORTFOLIO, pageable);
        } else {
            // 글 제목으로 검색
            if (searchType.equals("TITLE")) {
                board = boardRepository.findByCategoryAndTitleContains(Category.PORTFOLIO, keyword, pageable);
            }
            // 작성자 닉네임으로 검색
            else {
                board = boardRepository.findByCategoryAndUserNicknameContains(Category.PORTFOLIO, keyword, pageable);
            }
        }
        return BoardListResponse.toDtoList(board);
    }


    //포토폴리오 작성 + 자유게시판 작성
    @Transactional
    public Long writeWithFile(BoardCreateRequest req, MultipartFile file, String username, Category category) throws IOException {

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        String changedFile = null;

        if (file != null) {
            changedFile = uploadFile(file);
        }

        Board board = req.toEntity(user, changedFile, category);
        boardRepository.save(board);

        return board.getId();
    }

    //포토폴리오 수정
    @Transactional
    public void modify(BoardModifyRequest req, MultipartFile file, String username, Long boardId) throws IOException {

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
    @Transactional
    public BoardDto portfolioDetail(Long boardId, Boolean addView) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getCategory().name().equals("PORTFOLIO")) {
            throw new AppException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(addView) {
            board.addViews();
        }
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

    public BoardDto rankUpdate(BoardModifyRequest modifyRequest, MultipartFile file, String userName, Long boardId) throws IOException {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (!Objects.equals(board.getUser().getUserName(), user.getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        String changedFile = null;

        //만약 기존 게시글에 파일이 있던 경우
        if (board.getImage() !=  null) {
            if (modifyRequest.getImage().equals("changed")) { //파일 변경시
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

        board.modify(modifyRequest.getTitle(), modifyRequest.getContent(), changedFile);
        boardRepository.save(board);
        return BoardDto.of(board);

    }

    @Transactional
    public Long rankDelete(Long id, String userName){
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));
        Board board = boardRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (board.getUser().getUserName() != user.getUserName() && user.getRole() != UserRole.ADMIN){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        if (board.getImage() != null) {
            deleteFile(board.getImage());
        }

        boardRepository.deleteById(id);

        if (user.getRole() == UserRole.ADMIN) {
            alarmService.send(board.getUser(), AlarmType.REFUSED_PLANNER, "/", board.getTitle());
        }
        return id;
    }

    // 유저 신고 작성
    public Board reportWrite(ReportCreateRequest reportCreateRequest, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Board board = boardRepository.save(reportCreateRequest.toEntity(user));

        return Board.builder()
                .id(board.getId())
                .category(Category.REPORT)
                .title(reportCreateRequest.getTitle())
                .content(reportCreateRequest.getContent())
                .build();

    }

}