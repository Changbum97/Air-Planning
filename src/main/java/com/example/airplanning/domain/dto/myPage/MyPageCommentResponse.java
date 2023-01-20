package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.GenericDeclaration;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageCommentResponse {

    private Long id; //해당 댓글 id
    private CommentType commentType; // 댓글 타입
    private String content; // 댓글 내용
    private Long parentId; // 리뷰 or 글 id


    public static MyPageCommentResponse Of(Comment comment) {

        Long commentOrReviewId;

        //댓글이 리뷰에 달렸는지 게시글에 달렸는지 구분
        if (comment.getCommentType().equals(CommentType.BOARD_COMMENT)) {
            commentOrReviewId = comment.getBoard().getId();
        } else {
            commentOrReviewId = comment.getReview().getId();
        }

        return MyPageCommentResponse.builder()
                .id(comment.getId())
                .commentType(comment.getCommentType())
                .content(comment.getContent())
                .parentId(commentOrReviewId)
                .build();
    }

}