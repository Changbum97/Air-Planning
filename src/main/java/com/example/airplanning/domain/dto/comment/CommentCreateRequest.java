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
public class CommentCreateRequest {

    private String commentType;
    private String content;

    public Comment toBoardCommentEntity(User user, Board board) {
        return Comment.builder()
                .content(this.content)
                .user(user)
                .commentType(CommentType.BOARD_COMMENT)
                .board(board)
                .build();
    }
    public Comment toReviewCommentEntity(User user, Review review) {
        return Comment.builder()
                .content(this.content)
                .user(user)
                .commentType(CommentType.REVIEW_COMMENT)
                .review(review)
                .build();
    }
}