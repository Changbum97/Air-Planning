package com.example.airplanning.domain.dto.board;

import com.example.airplanning.domain.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;



@AllArgsConstructor
@Getter
public class BoardResponse {
    private String message;
    private Long boardId;
}

