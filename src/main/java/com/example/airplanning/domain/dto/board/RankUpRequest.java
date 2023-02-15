package com.example.airplanning.domain.dto.board;

import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RankUpRequest {
    private String userName;
    private String description;
    private String region;
    private Long boardId;
    private Integer amount;

    public Planner toEntity(User user, Region region) {

        return Planner.builder()
                .user(user)
                .description(this.description)
                .region(region)
                .amount(this.amount)
                .starSum(0)
                .reviewCount(0)
                .build();
    }
}
