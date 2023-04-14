package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.review.*;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@ApiIgnore
public class ReviewController {

    private final ReviewService reviewService;
    private final LikeService likeService;

    @GetMapping("/list")
    public String listReview(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                             Model model,
                             @RequestParam(required = false) String searchType,
                             @RequestParam(required = false) String keyword) {

        Page<ReviewListResponse> reviewPage = reviewService.reviewList(pageable, searchType, keyword);
        model.addAttribute("list", reviewPage);
        model.addAttribute("reviewSearchRequest", new ReviewSearchRequest(searchType, keyword));

        return "reviews/list";
    }

    @GetMapping("/write/{planId}")
    public String writeReviewPage(Model model, @PathVariable Long planId) {
        System.out.println("플래너아이디 : "+planId);
        model.addAttribute(new ReviewCreateRequest(planId));
        return "reviews/write";
    }

    @GetMapping("/{reviewId}/update")
    public String updateReviewPage(@PathVariable Long reviewId, Model model) {
        Review review = reviewService.findById(reviewId, false);
        model.addAttribute(new ReviewUpdateRequest(review.getTitle(), review.getContent(), review.getStar(), review.getImage()));
        return "reviews/update";
    }
    @GetMapping("/{reviewId}")
    public String getOneReview(@PathVariable Long reviewId, Model model, Principal principal,
                               HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        Cookie oldCookie = null;
        Cookie[] cookies = httpServletRequest.getCookies();
        Boolean addView = true;
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("reviewView")) {
                    oldCookie = cookie;
                    break;
                }
            }
        }
        if(oldCookie != null && oldCookie.getValue().equals(reviewId.toString())) {
            addView = false;
        } else {
            Cookie newCookie = new Cookie("reviewView", reviewId.toString());
            newCookie.setMaxAge(60 * 60);   // 한 시간
            httpServletResponse.addCookie(newCookie);
        }

        Review review = reviewService.findById(reviewId, addView);

        if (principal != null) {
            model.addAttribute("checkLike", likeService.checkReviewLike(reviewId, principal.getName()));
            model.addAttribute("userName", principal.getName());
            // 로그인 유저가 글 작성자라면 수정, 삭제 버튼 출력
            if(principal.getName().equals(review.getUser().getUserName())) {
                model.addAttribute("isWriter", true);
            }
        } else {
            model.addAttribute("checkLike", false);
        }

        model.addAttribute("review", ReviewDto.of(review));
        model.addAttribute("userName", null);
        return "reviews/detail";
    }
}
