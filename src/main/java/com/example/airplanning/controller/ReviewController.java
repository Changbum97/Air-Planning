package com.example.airplanning.controller;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.review.*;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

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

    @ResponseBody
    @PostMapping("")
    public String writeReview(ReviewCreateRequest request, Principal principal) {
        reviewService.write(request, principal.getName());
        return "리뷰 작성 성공";
    }

    @GetMapping("/{reviewId}/update")
    public String updateReviewPage(@PathVariable Long reviewId, Model model) {
        Review review = reviewService.findById(reviewId);
        model.addAttribute(new ReviewUpdateRequest(review.getTitle(), review.getContent(), review.getStar()));
        return "reviews/update";
    }

    @PostMapping("/{reviewId}/update")
    public String updateReview(@PathVariable Long reviewId, ReviewUpdateRequest request, Principal principal) {
        Long updatedReviewId = reviewService.update(reviewId, request, principal.getName());
        return "redirect:/reviews/{reviewId}";
    }

    @GetMapping("/{reviewId}")
    public String getOneReview(@PathVariable Long reviewId, Model model, Principal principal) {
        Review review = reviewService.findById(reviewId);
        model.addAttribute("review", ReviewDto.of(review));
        model.addAttribute("userName", principal.getName());
        return "reviews/detail";
    }

    @GetMapping("/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId, Principal principal) {
        System.out.println("삭제 요청 수락! : " +reviewId);
        reviewService.delete(reviewId, principal.getName());
        System.out.println("삭제 완료");
        return "redirect:";
    }
}
