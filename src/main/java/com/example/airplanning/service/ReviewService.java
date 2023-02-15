package com.example.airplanning.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.airplanning.domain.dto.review.ReviewCreateRequest;
import com.example.airplanning.domain.dto.review.ReviewListResponse;
import com.example.airplanning.domain.dto.review.ReviewUpdateRequest;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.PlannerRepository;
import com.example.airplanning.repository.ReviewRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final PlannerRepository plannerRepository;

    private final PlanRepository planRepository;

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    // 알람 테스트
    private final AlarmService alarmService;

    public Page<ReviewListResponse> reviewList(Pageable pageable, String searchType, String keyword) {
        Page<Review> reviews;

        if (searchType == null) {
            reviews = reviewRepository.findAll(pageable);
        } else {
            if (searchType.equals("TITLE")) {
                reviews = reviewRepository.findAllByTitleContains(keyword, pageable);
            } else {
                reviews = reviewRepository.findAllByPlannerUserNicknameContains(keyword, pageable);
            }
        }
        return reviews.map(review -> ReviewListResponse.of(review));
    }


    @Transactional
    public Long write(ReviewCreateRequest request, MultipartFile file, String userName) throws IOException {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        String changedFile = null;

        if (file != null) {
            changedFile = uploadFile(file);
        }

        Review savedReview = reviewRepository.save(request.toEntity(user, plan.getPlanner(), changedFile));

        plan.reviewedPlan();
        planRepository.save(plan);

        alarmService.send(plan.getPlanner().getUser(), AlarmType.REVIEW_ALARM, "/reviews/"+savedReview.getId(), savedReview.getTitle());
        return savedReview.getId();
    }

    @Transactional
    public Long update(Long reviewId, ReviewUpdateRequest request, MultipartFile file, String userName) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!userName.equals(review.getUser().getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        String changedFile = null;

        //만약 기존 게시글에 파일이 있던 경우
        if (review.getImage() !=  null) {
            if (request.getImage().equals("changed")) { //파일 변경시
                if (file != null) { // 파일을 다른 파일로 교체한 경우
                    changedFile = uploadFile(file);
                    deleteFile(review.getImage()); //기존 파일 삭제
                } else { //파일 삭제한 경우
                    deleteFile(review.getImage()); //기존 파일 삭제
                }
            } else { //파일 변경이 없던 경우
                changedFile = review.getImage();
            }
        } else { //기존 파일이 없던 경우
            if (file != null) { //새 파일 업로드
                changedFile = uploadFile(file);
            }
        }

        review.update(request, changedFile);
        Review updatedReview = reviewRepository.save(review);
        return updatedReview.getId();
    }

    @Transactional
    public void delete(Long reviewId, String userName) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!userName.equals(review.getUser().getUserName())) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        if (review.getImage() != null) {
            deleteFile(review.getImage());
        }

        reviewRepository.delete(review);
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

    @Transactional
    public Review findById(Long reviewId, Boolean addView) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if(addView) {
            review.addViews();
        }

        return review;
    }
}
