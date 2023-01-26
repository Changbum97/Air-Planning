package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.dto.review.ReviewDto;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.service.ReviewService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/write")
    public String writeReviewPage(Model model) {
        model.addAttribute(new ReviewCreateRequest());
        return "reviews/write";
    }

    @ResponseBody
    @PostMapping("")
    public String writeReview(ReviewCreateRequest request, Principal principal) {
        reviewService.write(request, principal.getName());
        return "리뷰 작성 성공";
    }

    @GetMapping("/{reviewId}")
    public String getOneReview(@PathVariable Long reviewId, Model model) {
        Review review = reviewService.findById(reviewId);
        model.addAttribute("review", ReviewDto.of(review));
        return "reviews/detail";
    }
}
