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
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageBoardResponse {

    private Long id; //해당 게시글 id
    private String title; //해당 게시글 제목
    private String category;
    private String createdAt;

    public static MyPageBoardResponse Of(Board board) {

        String titleSub;

        if (board.getTitle().length() <= 10) {
            titleSub = board.getTitle();
        } else {
            titleSub = board.getTitle().substring(0,10)+" ...";
        }

        String category = "";
        if (board.getCategory().name().equals("FREE")) {
            category = "자유게시판";
        } else if (board.getCategory().name().equals("RANK_UP")) {
            category = "등업게시판";
        } else if (board.getCategory().name().equals("REPORT")) {
            category = "신고게시판";
        } else if (board.getCategory().name().equals("PORTFOLIO")) {
            category = "포폴게시판";
        }
        return MyPageBoardResponse.builder()
                .id(board.getId())
                .title(titleSub)
                .category(category)
                .createdAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }

    public static MyPageBoardResponse Of(Like like) {

        String titleSub;

        if (like.getBoard().getTitle().length() <= 10) {
            titleSub = like.getBoard().getTitle();
        } else {
            titleSub = like.getBoard().getTitle().substring(0,10)+" ...";
        }

        String category = "";
        if (like.getBoard().getCategory().name().equals("FREE")) {
            category = "자유게시판";
        } else if (like.getBoard().getCategory().name().equals("RANK_UP")) {
            category = "등업게시판";
        } else if (like.getBoard().getCategory().name().equals("REPORT")) {
            category = "신고게시판";
        } else if (like.getBoard().getCategory().name().equals("PORTFOLIO")) {
            category = "포폴게시판";
        }

        return MyPageBoardResponse.builder()
                .id(like.getBoard().getId())
                .title(titleSub)
                .category(category)
                .createdAt(like.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }

}
