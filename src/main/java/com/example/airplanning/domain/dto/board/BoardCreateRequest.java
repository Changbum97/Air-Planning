package com.example.airplanning.domain.dto.board;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import lombok.*;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class BoardCreateRequest {
    private String title;
    private String content;

    private Category category;

    public Board toEntity(User user){
        return Board.builder()
                .user(user)
                .title(this.title)
                .content(this.content)
                .category(Category.FREE)
                .region(user.getPlanner().getRegion())
                .views(0)
                .build();
    }

    public Board toEntity(User user, String image, Category category){
        return Board.builder()
                .user(user)
                .title(this.title)
                .content(this.content)
                .image(image)
                .category(category)
                .image(image)
                .region(user.getPlanner().getRegion())
                .views(0)
                .build();
    }
}
