package com.example.airplanning.service;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;

    //마이페이지 내정보
    public MyPageInfoResponse getMyPageInfo(String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        return MyPageInfoResponse.of(user);
    }

    //마이페이지 내가 쓴 글
    public Page<MyPageBoardResponse> getMyBoard(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Board> boardPages = boardRepository.findAllByUser(user, pageable);

        return new PageImpl<>(boardPages.stream()
                .map(Board -> MyPageBoardResponse.Of(Board))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 쓴 리뷰
    public Page<MyPageReviewResponse> getMyReview(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Review> reviewPages = reviewRepository.findAllByUser(user, pageable);

        return new PageImpl<>(reviewPages.stream()
                .map(Review -> MyPageReviewResponse.Of(Review))
                .collect(Collectors.toList()));

    }

    //마이페이지 내가 쓴 댓글
    public Page<MyPageCommentResponse> getMyComment(Pageable pageable, String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Page<Comment> commentPages = commentRepository.findAllByUser(user, pageable);

        return new PageImpl<>(commentPages.stream()
                .map(Comment -> MyPageCommentResponse.Of(Comment))
                .collect(Collectors.toList()));

    }

}
