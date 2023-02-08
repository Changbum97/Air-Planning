package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private String nickname;
    private String createdAt;
    private String deletedAt;
    private Long parentId;
    private List<CommentResponse> coComment;

    // 부모 댓글
    public static CommentResponse of(Comment comment) {
        List<CommentResponse> coCo = null;
        if (comment.getChildren() != null) {
            coCo = comment.getChildren().stream().map(coComment->CommentResponse.of2(coComment)).collect(Collectors.toList());
        }
        String deletedAt = null;
        if (comment.getDeletedAt() != null) deletedAt = comment.getDeletedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getUserName())
                .nickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .deletedAt(deletedAt)
                .coComment(coCo)
                .build();
    }

    // 자식 댓글
    public static CommentResponse of2(Comment comment) {
        String deletedAt = null;
        if (comment.getDeletedAt() != null) deletedAt = comment.getDeletedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getUserName())
                .nickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .deletedAt(deletedAt)
                .build();
    }

    public static CommentResponse ofCo(Comment comment) {
        String deletedAt = null;
        if (comment.getDeletedAt() != null) deletedAt = comment.getDeletedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getUserName())
                .nickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .deletedAt(deletedAt)
                .parentId(comment.getParent().getId())
                .build();
    }
}
