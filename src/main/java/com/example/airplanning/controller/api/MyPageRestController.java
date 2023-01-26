package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/{userId}/mypage")
@RequiredArgsConstructor
public class MyPageRestController {

    private final MyPageService myPageService;

    //마이페이지 내 정보
    @GetMapping("/info")
    public ResponseEntity<MyPageInfoResponse> getMyInfo(@PathVariable Long userId, Principal principal) {
        MyPageInfoResponse myPageInfoDto = myPageService.getMyPageInfo(principal.getName());
        return ResponseEntity.ok().body(myPageInfoDto);
    }

    //마이페이지 내가 쓴 글
    @GetMapping("/my/boards")
    public ResponseEntity<Page<MyPageBoardResponse>> getMyBoard(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                @PathVariable Long userId, Principal principal) {
        Page<MyPageBoardResponse> boardDtos = myPageService.getMyBoard(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 내가 쓴 리뷰
    @GetMapping("/my/reviews")
    public ResponseEntity<Page<MyPageReviewResponse>> getMyReview(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getMyReview(pageable, principal.getName());
        return ResponseEntity.ok().body(reviewDtos);

    }

    //마이페이지 내가 쓴 댓글
    @GetMapping("/my/comments")
    public ResponseEntity<Page<MyPageCommentResponse>> getMyComment(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPageCommentResponse> commentDtos = myPageService.getMyComment(pageable, principal.getName());
        return ResponseEntity.ok().body(commentDtos);

    }

    //마이페이지 내가 좋아요 한 게시글
    @GetMapping("/like/boards")
    public ResponseEntity<Page<MyPageBoardResponse>> getLikeBoard(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPageBoardResponse> boardDtos = myPageService.getLikeBoard(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 내가 좋아요 한 리뷰
    @GetMapping("/like/reviews")
    public ResponseEntity<Page<MyPageReviewResponse>> getLikeReview(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getLikeReview(pageable, principal.getName());
        return ResponseEntity.ok().body(reviewDtos);

    }

    //마이페이지 내가 좋아요 한 플래너
    @GetMapping("/like/planners")
    public ResponseEntity<Page<MyPagePlannerResponse>> getLikePlanner(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @PathVariable Long userId, Principal principal) {
        Page<MyPagePlannerResponse> boardDtos = myPageService.getLikePlanner(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 여행중
    @GetMapping("/trip/progress")
    public ResponseEntity<Page<MyPagePlanResponse>> getProgressPlan(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPagePlanResponse> planDtos = myPageService.getProgressPlan(pageable, principal.getName());
        return ResponseEntity.ok().body(planDtos);
    }

    //마이페이지 여행완료
    @GetMapping("/trip/finish")
    public ResponseEntity<Page<MyPagePlanResponse>> getFinishPlan(@PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPagePlanResponse> planDtos = myPageService.getFinishPlan(pageable, principal.getName());
        return ResponseEntity.ok().body(planDtos);
    }
}
