package com.example.airplanning.controller.api;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.service.MyPageService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/{userId}/mypage")
@RequiredArgsConstructor
public class MyPageRestController {

    private final MyPageService myPageService;

    //마이페이지 내 정보
    @GetMapping("/info")
    @ApiOperation(value = "유저 정보 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<MyPageInfoResponse> getMyInfo(@PathVariable Long userId, @ApiIgnore Principal principal) {
        MyPageInfoResponse myPageInfoDto = myPageService.getMyPageInfo(principal.getName());
        return ResponseEntity.ok().body(myPageInfoDto);
    }

    //마이페이지 내가 쓴 글
    @GetMapping("/my/boards")
    @ApiOperation(value = "유저가 작성한 게시글 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPageBoardResponse>> getMyBoard(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPageBoardResponse> boardDtos = myPageService.getMyBoard(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 내가 쓴 리뷰
    @GetMapping("/my/reviews")
    @ApiOperation(value = "유저가 작성한 리뷰 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPageReviewResponse>> getMyReview(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getMyReview(pageable, principal.getName());
        return ResponseEntity.ok().body(reviewDtos);

    }

    //마이페이지 내가 쓴 댓글
    @GetMapping("/my/comments")
    @ApiOperation(value = "유저가 작성한 댓글 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPageCommentResponse>> getMyComment(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPageCommentResponse> commentDtos = myPageService.getMyComment(pageable, principal.getName());
        return ResponseEntity.ok().body(commentDtos);

    }

    //마이페이지 내가 좋아요 한 게시글
    @GetMapping("/like/boards")
    @ApiOperation(value = "유저가 좋아요 누른 게시글 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPageBoardResponse>> getLikeBoard(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPageBoardResponse> boardDtos = myPageService.getLikeBoard(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 내가 좋아요 한 리뷰
    @GetMapping("/like/reviews")
    @ApiOperation(value = "유저가 좋아요 누른 리뷰 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPageReviewResponse>> getLikeReview(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getLikeReview(pageable, principal.getName());
        return ResponseEntity.ok().body(reviewDtos);

    }

    //마이페이지 내가 좋아요 한 플래너
    @GetMapping("/like/planners")
    @ApiOperation(value = "유저가 좋아요 누른 플래너 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPagePlannerResponse>> getLikePlanner(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPagePlannerResponse> boardDtos = myPageService.getLikePlanner(pageable, principal.getName());
        return ResponseEntity.ok().body(boardDtos);

    }

    //마이페이지 여행중
    @GetMapping("/trip/progress")
    @ApiOperation(value = "유저의 '여행중' 상태인 플랜 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPagePlanResponse>> getProgressPlan(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPagePlanResponse> planDtos = myPageService.getProgressPlan(pageable, principal.getName());
        return ResponseEntity.ok().body(planDtos);
    }

    //마이페이지 여행완료
    @GetMapping("/trip/finish")
    @ApiOperation(value = "유저의 '여행완료' 상태인 플랜 확인")
    @ApiImplicitParam(name = "userId", value = "로그인 상태에서 유저 ID 번호 입력")
    public ResponseEntity<Page<MyPagePlanResponse>> getFinishPlan(@ApiIgnore @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, @ApiIgnore Principal principal) {
        Page<MyPagePlanResponse> planDtos = myPageService.getFinishPlan(pageable, principal.getName());
        return ResponseEntity.ok().body(planDtos);
    }
}
