package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.enum_class.Category;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    private LocalDateTime createdAt; // 등록 날짜

    @Enumerated(EnumType.STRING)
    private Category category;

    public static BoardDto of (Board board){
        return BoardDto.builder()
                .id(board.getId())
                .userName(board.getUser().getUserName())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .category(board.getCategory())
                .build();
    }


}
