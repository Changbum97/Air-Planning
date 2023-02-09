package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CoCommentCreateRequest2 {
    private Long parentId;
    private Long postId;
    private String content;
    private String commentType;

    public Comment toBoardCoCommentEntity(User user, Board board, Comment parentComment) {
        return Comment.builder()
                .content(this.content)
                .user(user)
                .commentType(CommentType.BOARD_COMMENT)
                .board(board)
                .parent(parentComment)
                .build();
    }

    public Comment toReviewCoCommentEntity(User user, Review review, Comment parentComment) {
        return Comment.builder()
                .content(this.content)
                .user(user)
                .commentType(CommentType.REVIEW_COMMENT)
                .review(review)
                .parent(parentComment)
                .build();
    }
}
