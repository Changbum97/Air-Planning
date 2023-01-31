package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentCreateRequest {
    private String content;

    public Comment toEntity(User user, Board board, String commentType) {
        CommentType type = null;
        if (commentType.equals(CommentType.BOARD_COMMENT.name())) {
            type = CommentType.BOARD_COMMENT;
        } else {
            type = CommentType.REVIEW_COMMENT;
        }
        return Comment.builder()
                .content(this.content)
                .user(user)
                .commentType(type)
                .board(board)
                .build();
    }
}
