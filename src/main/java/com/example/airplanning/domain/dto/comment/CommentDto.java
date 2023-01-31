package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CommentDto {

    private Long userId;
    private String content;
    private CommentType commentType;
    private Long reviewId;
    private Long boardId;

    public static CommentDto of(Comment comment) {
        return CommentDto.builder()
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .build();
    }
}
