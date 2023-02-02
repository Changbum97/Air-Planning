package com.example.airplanning.domain.dto.comment;

import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.enum_class.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.util.annotation.Nullable;

import java.security.PrivateKey;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CommentDtoWithCoCo {

    private Long id;
    private Long userId;
    private String nickname;
    private CommentType commentType;
    private Long postId;
    @Nullable
    private Long parentCommentId;
    private String content;
    private String createdAt;
    private List<CommentDto> coComment;

    public static CommentDtoWithCoCo of(Comment comment) {
        Long postId = 0L;
        if (comment.getBoard() != null) {
            postId = comment.getBoard().getId();
        } else {
            postId = comment.getReview().getId();
        }
        List<CommentDto> coCo = comment.getChildren().stream().map(coComment -> CommentDto.of(coComment)).collect(Collectors.toList());
        return CommentDtoWithCoCo.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .postId(postId)
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .coComment(coCo)
                .build();
    }

    public static CommentDtoWithCoCo ofCo(Comment comment) {
        Long postId = 0L;
        if (comment.getBoard() != null) {
            postId = comment.getBoard().getId();
        } else {
            postId = comment.getReview().getId();
        }
        return CommentDtoWithCoCo.builder()
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")))
                .postId(postId)
                .parentCommentId(comment.getParent().getId())
                .build();
    }
}
