package com.example.airplanning.domain.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDto {
    private Long id;
    private String userName;
    private String title;
    private String content;
    private LocalDate registeredAt;
}
