package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.myPage.*;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.MyPageService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/users/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;
    private final UserService userService;

    @GetMapping("")
    public String myPage(Principal principal, Model model) {

        UserDto user = userService.findUser(principal.getName());

        model.addAttribute("user", user);

        return "users/myPage";

    }

    //마이페이지 비밀번호 확인
    @GetMapping("/{userId}/edit/password")
    public String passwordPage(@PathVariable Long userId, Model model) {

        model.addAttribute("userId", userId);

        return "users/myPagePassword";
    }

    @ResponseBody
    @GetMapping("/{userId}/check-password")
    public String checkPassword(@PathVariable Long userId, @RequestParam String password) {

        try {
            UserDto user = userService.findUserById(userId);
            userService.checkPassword(user.getUserName(), password);
        } catch (AppException e) {
            //user가 찾아지지 않을 때
            if(e.getErrorCode().equals(ErrorCode.USER_NOT_FOUNDED)) {
                return "1";
            }
            // 비밀번호 오류
            else if(e.getErrorCode().equals(ErrorCode.INVALID_PASSWORD)) {
                return "2";
            }
        }

        //성공
        return "3";
    }

    //마이페이지 수정
    @GetMapping("/{userId}/edit")
    public String editPage(Model model, @PathVariable Long userId, Principal principal) {

        UserDto user = userService.findUserById(userId);

        // 다른 사람의 정보 수정 페이지에 진입한 경우
        if (!principal.getName().equals(user.getUserName())) {
            model.addAttribute("msg", "내 정보만 수정가능합니다.");
            model.addAttribute("nextPage", "/users/mypage");
            return "error/redirect";
        }

        model.addAttribute("user", user);
        model.addAttribute("myPageEditRequest", new MyPageEditRequest());

        return "users/myPageEdit";
    }


    //마이페이지 정보 수정 완료
    @ResponseBody
    @PostMapping("/{userId}/edit")
    public String editInfo(@PathVariable Long userId, @RequestPart(value = "request") MyPageEditRequest req,
                           @RequestPart(value = "img",required = false) MultipartFile file, Principal principal) throws IOException {
        
        try {
            userService.editUserInfo(req.getPassword(), req.getNickname(), file, principal.getName());
        } catch (AppException e) {
            if  (e.getErrorCode().equals(ErrorCode.FILE_UPLOAD_ERROR)) { //S3 업로드 오류
                return "파일 업로드 과정 중 오류가 발생했습니다. 다시 시도 해주세요.*/mypage/"+userId+"/edit";
            }
        } catch (Exception e) { //principal 오류 (로그인 오류, 만료)
            return "로그인 정보가 유효하지 않습니다. 다시 로그인 해주세요.*/login";
        }

        return "변경이 완료되었습니다.*/mypage";
    }

    //마이페이지 여행중
    @ResponseBody
    @GetMapping("/{userId}/trip/progress")
    //size max = 2000, 진짜 Integer.Max로 하려면 configuration필요
    public List<MyPagePlanResponse> getProgressPlan(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                    @PathVariable Long userId, Principal principal) {

        Page<MyPagePlanResponse> planDtos = myPageService.getProgressPlan(pageable, principal.getName());
        return planDtos.getContent();
    }

    //마이페이지 여행완료
    @ResponseBody
    @GetMapping("/{userId}/trip/finish")
    public List<MyPagePlanResponse> getFinishPlan(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, Principal principal) {
        Page<MyPagePlanResponse> planDtos = myPageService.getFinishPlan(pageable, principal.getName());
        return planDtos.getContent();
    }


    //마이페이지 내가 쓴 글
    @ResponseBody
    @GetMapping("/{userId}/my/boards")
    public List<MyPageBoardResponse> getMyBoard(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                @PathVariable Long userId, Principal principal)  {

        Page<MyPageBoardResponse> boardDtos = myPageService.getMyBoard(pageable, principal.getName());
        return boardDtos.getContent();

    }

    //마이페이지 내가 쓴 리뷰
    @ResponseBody
    @GetMapping("/{userId}/my/reviews")
    public List<MyPageReviewResponse> getMyReview(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getMyReview(pageable, principal.getName());
        return reviewDtos.getContent();

    }

    //마이페이지 내가 쓴 댓글
    @ResponseBody
    @GetMapping("/{userId}/my/comments")
    public List<MyPageCommentResponse> getMyComment(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPageCommentResponse> commentDtos = myPageService.getMyComment(pageable, principal.getName());
        return commentDtos.getContent();

    }


    //마이페이지 내가 좋아요 한 게시글
    @ResponseBody
    @GetMapping("/{userId}/like/boards")
    public List<MyPageBoardResponse> getLikeBoard(@PageableDefault(size = Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @PathVariable Long userId, Principal principal) {
        Page<MyPageBoardResponse> boardDtos = myPageService.getLikeBoard(pageable, principal.getName());
        return boardDtos.getContent();

    }

    //마이페이지 내가 좋아요 한 리뷰
    @ResponseBody
    @GetMapping("/{userId}/like/reviews")
    public List<MyPageReviewResponse> getLikeReview(@PageableDefault(size=Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @PathVariable Long userId, Principal principal) {
        Page<MyPageReviewResponse> reviewDtos = myPageService.getLikeReview(pageable, principal.getName());
        return reviewDtos.getContent();

    }

    //마이페이지 내가 좋아요 한 플래너
    @ResponseBody
    @GetMapping("/{userId}/like/planners")
    public List<MyPagePlannerResponse> getLikePlanner(@PageableDefault(size=Integer.MAX_VALUE, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @PathVariable Long userId, Principal principal) {
        Page<MyPagePlannerResponse> boardDtos = myPageService.getLikePlanner(pageable, principal.getName());
        return boardDtos.getContent();

    }


}
