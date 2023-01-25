package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class BoardCreateRequest {
    private String title;
    private String content;

    public Board of(User user){
        return Board.builder()
                .user(user)
                .title(this.title)
                .content(this.content)
                .build();
    }
}
