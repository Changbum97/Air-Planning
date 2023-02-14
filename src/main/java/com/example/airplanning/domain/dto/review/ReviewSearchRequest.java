package com.example.airplanning.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSearchRequest {
    private String searchType;
    private String keyword;
}
