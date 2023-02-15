package com.example.airplanning.domain.dto.board;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReportModifyRequest {
    private String title;
    private String content;
    private String image;
}
