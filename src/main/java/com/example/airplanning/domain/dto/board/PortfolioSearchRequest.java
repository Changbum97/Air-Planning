package com.example.airplanning.domain.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSearchRequest {
    private String searchType;
    private String keyword;
    private Long regionId;
}
