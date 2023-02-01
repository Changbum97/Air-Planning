package com.example.airplanning.domain.dto.board;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class BoardModifyRequest {
    private String title;
    private String content;
}

