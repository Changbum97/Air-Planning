package com.example.airplanning.domain.dto.board;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankUpDetailResponse {

    private Long id;
    private String userName;
    private String nickname;
    private LocalDateTime createdAt;
    private String region;
    private String title;
    private String content;

    public static RankUpDetailResponse of(Board board) {
        return RankUpDetailResponse.builder()
                .id(board.getId())
                .userName(board.getUser().getUserName())
                .nickname(board.getUser().getNickname())
                .createdAt(board.getCreatedAt())
                .title(board.getTitle())
                .content(board.getContent())
                .region(board.getRegion().getRegion1() + " " + board.getRegion().getRegion2())
                .build();
    }
}
