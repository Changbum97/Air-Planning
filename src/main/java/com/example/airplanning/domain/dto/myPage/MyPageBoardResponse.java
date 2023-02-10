package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageBoardResponse {

    private Long id; //해당 게시글 id
    private String title; //해당 게시글 제목
    private Category category;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static MyPageBoardResponse Of(Board board) {

        String titleSub;

        if (board.getTitle().length() <= 10) {
            titleSub = board.getTitle();
        } else {
            titleSub = board.getTitle().substring(0,10)+" ...";
        }

        return MyPageBoardResponse.builder()
                .id(board.getId())
                .title(titleSub)
                .category(board.getCategory())
                .createdAt(board.getCreatedAt())
                .build();
    }

    public static MyPageBoardResponse Of(Like like) {

        String titleSub;

        if (like.getBoard().getTitle().length() <= 10) {
            titleSub = like.getBoard().getTitle();
        } else {
            titleSub = like.getBoard().getTitle().substring(0,10)+" ...";
        }

        return MyPageBoardResponse.builder()
                .id(like.getBoard().getId())
                .title(titleSub)
                .category(like.getBoard().getCategory())
                .createdAt(like.getCreatedAt())
                .build();
    }

}
