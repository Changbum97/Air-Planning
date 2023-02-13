package com.example.airplanning.domain.dto.board;

import com.example.airplanning.domain.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardListResponse {

    private Long id;
    private String nickname;    // 작성자 닉네임
    private String title;       // 제목
    private String createdAt;   // 등록 날짜

    public static BoardListResponse of (Board board){
        String title = board.getTitle();
        if(title.length() > 10) {
            title = title.substring(0, 10) + "...";
        }
        String nickname = board.getUser().getNickname();
        if(nickname.length() > 10) {
            nickname = nickname.substring(0, 10) + "...";
        }

        return BoardListResponse.builder()
                .id(board.getId())
                .nickname(nickname)
                .title(title)
                .createdAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }

    public static Page<BoardListResponse> toDtoList(Page<Board> boards){
        return boards.map(board -> BoardListResponse.of(board));
    }

}
