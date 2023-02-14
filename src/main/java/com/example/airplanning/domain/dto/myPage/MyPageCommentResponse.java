package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.CommentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.GenericDeclaration;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageCommentResponse {

    private Long id; //해당 댓글 id
    private String content; // 댓글 내용
    private Long parentId; // 리뷰 or 글 id
    private String parentType;
    private String parentTitle; // 리뷰 or 글의 제목
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;


    public static MyPageCommentResponse Of(Comment comment) {

        Long commentOrReviewId;
        String commentOrReviewTitle;
        String contentSub;
        String contentType;

        //댓글이 리뷰에 달렸는지 게시글에 달렸는지 구분
        if (comment.getCommentType().equals(CommentType.BOARD_COMMENT)) {
            commentOrReviewId = comment.getBoard().getId();
            commentOrReviewTitle = comment.getBoard().getTitle();
            contentType = "자유게시판";
        } else {
            commentOrReviewId = comment.getReview().getId();
            commentOrReviewTitle = comment.getReview().getTitle();
            contentType = "리뷰게시판";
        }

        if (comment.getContent().length() <= 10) {
            contentSub = comment.getContent();
        } else {
            contentSub = comment.getContent().substring(0,10)+" ...";
        }

        return MyPageCommentResponse.builder()
                .id(comment.getId())
                .content(contentSub)
                .parentId(commentOrReviewId)
                .parentType(contentType)
                .parentTitle(commentOrReviewTitle)
                .createdAt(comment.getCreatedAt())
                .build();
    }

}