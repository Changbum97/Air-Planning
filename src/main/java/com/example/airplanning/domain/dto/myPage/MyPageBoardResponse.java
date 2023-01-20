package com.example.airplanning.domain.dto.myPage;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.enum_class.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MyPageBoardResponse {

    private Long id; //해당 게시글 id
    private String title; //해당 게시글 제목
    private Category category;

    public static MyPageBoardResponse Of(Board board) {
        return MyPageBoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .category(board.getCategory())
                .build();
    }

    public static MyPageBoardResponse Of(Like like) {
        return MyPageBoardResponse.builder()
                .id(like.getBoard().getId())
                .title(like.getBoard().getTitle())
                .category(like.getBoard().getCategory())
                .build();
    }

}
