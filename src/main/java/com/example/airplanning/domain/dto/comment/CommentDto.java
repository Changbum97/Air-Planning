package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.*;
import reactor.util.annotation.Nullable;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CommentDto {

    private Long id;
    private Long userId;
    private String nickname;
    private CommentType commentType;
    private Long postId;
    @Nullable
    private Long parentCommentId;
    private String content;
    private String createdAt;

    public static CommentDto of(Comment comment) {
        Long postId = 0L;
        if (comment.getBoard() != null) {
            postId = comment.getBoard().getId();
        } else {
            postId = comment.getReview().getId();
        }
        return CommentDto.builder()
                .id(comment.getId())
                .nickname(comment.getUser().getNickname())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .postId(postId)
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .build();
    }

    public static CommentDto ofCo(Comment comment) {
        Long postId = 0L;
        if (comment.getBoard() != null) {
            postId = comment.getBoard().getId();
        } else {
            postId = comment.getReview().getId();
        }
        return CommentDto.builder()
                .id(comment.getId())
                .nickname(comment.getUser().getNickname())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .postId(postId)
                .parentCommentId(comment.getParent().getId())
                .build();
    }
}
