package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class CommentCreateRequest2 {
    private Long boardId;
    private String content;

    public Comment toBoardCommentEntity(User user, Board board) {
        return Comment.builder()
                .content(this.content)
                .commentType(CommentType.BOARD_COMMENT)
                .user(user)
                .board(board)
                .build();
    }
}
