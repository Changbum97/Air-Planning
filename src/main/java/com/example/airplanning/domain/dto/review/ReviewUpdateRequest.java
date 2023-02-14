package com.example.airplanning.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReviewUpdateRequest {
    private String title;
    private String content;
    private Integer star;
    private String image;
}
