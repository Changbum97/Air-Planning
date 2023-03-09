package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.review.*;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Api("Review Controller")
public class ReviewRestController {

    private final ReviewService reviewService;
    private final LikeService likeService;

    @PostMapping
    @ApiOperation(value = "리뷰 작성", notes = "플랜 결제가 완료된 유저만 리뷰를 작성할 수 있습니다.")
    public Response<ReviewResponse> create(@RequestPart(value = "request") ReviewCreateRequest request,
                                      @RequestPart(value = "file",required = false) MultipartFile file, @ApiIgnore Principal principal) throws IOException {
        ReviewResponse result = reviewService.write(request, file, principal.getName());
        return Response.success(result);
    }

    @GetMapping("/{reviewId}")
    @ApiOperation(value = "리뷰 상세 조회", notes = "리뷰의 상세 내용을 확인합니다. 누구나 조회 가능합니다.")
    @ApiImplicitParam(name = "reviewId", value = "리뷰 번호", defaultValue = "None")
    public Response<ReviewResponse> read(@PathVariable Long reviewId, @ApiIgnore Principal principal,
                                         @ApiIgnore HttpServletResponse httpServletResponse, @ApiIgnore HttpServletRequest httpServletRequest) {
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
        return Response.success(ReviewResponse.of(review));
    }

    @GetMapping("/list")
    @ApiOperation(value = "리뷰 목록 조회", notes = "리뷰 목록을 확인합니다. 누구나 조회 가능하며, 리뷰 제목과 플래너 닉네임으로 검색 할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchType", value = "검색 조건, TITLE, NICKNAME 중 하나를 입력", defaultValue = "TITLE"),
            @ApiImplicitParam(name = "keyword", value = "검색어", defaultValue = "None")})
    public Response<Page<ReviewListResponse>> readList (@ApiIgnore @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                        @RequestParam(required = false) String searchType,
                                                        @RequestParam(required = false) String keyword) {
        Page<ReviewListResponse> reviewPage = reviewService.reviewList(pageable, searchType, keyword);
        return Response.success(reviewPage);
    }

    @PutMapping("/{reviewId}")
    @ApiOperation(value = "리뷰 수정", notes = "리뷰를 수정합니다. 해당 리뷰를 작성한 사용자만 수정이 가능합니다.")
    @ApiImplicitParam(name = "reviewId", value = "리뷰 번호", defaultValue = "None")
    public Response<ReviewResponse> update(@PathVariable Long reviewId,
                                           @RequestPart(value = "request") ReviewUpdateRequest request,
                                           @RequestPart(value = "file",required = false) MultipartFile file, @ApiIgnore Principal principal) throws IOException {
        ReviewResponse updatedReview = reviewService.update(reviewId, request, file, principal.getName());
        return Response.success(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    @ApiOperation(value = "리뷰 삭제", notes = "리뷰를 삭제합니다. 해당 리뷰를 작성한 사용자와 관리자만 삭제가 가능합니다.")
    @ApiImplicitParam(name = "reviewId", value = "리뷰 번호", defaultValue = "None")
    public Response<String> delete(@PathVariable Long reviewId, @ApiIgnore Principal principal) {
        Long deletedReviewId = reviewService.delete(reviewId, principal.getName());
        return Response.success(deletedReviewId+"번 리뷰가 삭제되었습니다.");
    }

    @PostMapping("/{reviewId}/like")
    @ApiOperation(value = "리뷰 좋아요 누르기", notes = "리뷰에 좋아요를 누릅니다. 로그인한 유저는 모두 누르기가 가능합니다")
    @ApiImplicitParam(name = "reviewId", value = "리뷰 번호", defaultValue = "None")
    public Response<String> Like(@PathVariable Long reviewId, @ApiIgnore Principal principal) {
        String result = likeService.changeLike(reviewId, principal.getName(), LikeType.REVIEW_LIKE);
        return Response.success(result);
    }
}
