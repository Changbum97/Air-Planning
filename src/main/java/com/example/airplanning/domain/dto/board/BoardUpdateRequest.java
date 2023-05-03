package com.example.airplanning.domain.dto.board;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class BoardUpdateRequest {
    private String title;
    private String content;
    private String image;
    private Long regionId;
    private Integer amount;

    public BoardUpdateRequest(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    public BoardUpdateRequest(String title, String content, String image, Long regionId, Integer amount) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.regionId = regionId;
        this.amount = amount;
    }
}

