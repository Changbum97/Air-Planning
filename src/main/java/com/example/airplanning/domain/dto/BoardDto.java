package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.Board;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDto {
    private Long id;
    private String userName;    // 로그인 ID
    private String title;       // 제목
    private String content;     // 내용
    private String image;       //파일 경로
    private LocalDateTime createdAt; // 등록 날짜
    private Integer likeCnt;    // 좋아요 개수

    public static BoardDto of (Board board){
        return BoardDto.builder()
                .id(board.getId())
                .userName(board.getUser().getUserName())
                .title(board.getTitle())
                .content(board.getContent())
                .image(board.getImage())
                .createdAt(board.getCreatedAt())
                .likeCnt(board.getLikes().size())
                .build();
    }

    public static Page<BoardDto> toDtoList(Page<Board> boards){
        Page<BoardDto> boardDtoList = boards.map(m -> BoardDto.builder()
                .id(m.getId())
                .title(m.getTitle())
                .content(m.getContent())
                .userName(m.getUser().getUserName())
                .createdAt(m.getCreatedAt())
                .build());
        return boardDtoList;
    }

}
