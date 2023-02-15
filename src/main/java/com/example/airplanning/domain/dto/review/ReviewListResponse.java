package com.example.airplanning.domain.dto.review;

import com.example.airplanning.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class ReviewListResponse {

    private Long id;
    private String nickname;    // 작성자 닉네임
    private String pNickname; // 플래너 닉네임
    private String title;       // 제목
    private String createdAt;   // 등록 날짜

    public static ReviewListResponse of (Review review) {
        String title = review.getTitle();
        if(title.length() > 10) {
            title = title.substring(0, 10) + "...";
        }
        String nickname = review.getUser().getNickname();
        if(nickname.length() > 10) {
            nickname = nickname.substring(0, 10) + "...";
        }
        String pNickname = review.getPlanner().getUser().getNickname();
        if(pNickname.length() > 10) {
            pNickname = pNickname.substring(0, 10) + "...";
        }

        return ReviewListResponse.builder()
                .id(review.getId())
                .nickname(nickname)
                .pNickname(pNickname)
                .title(title)
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")))
                .build();
    }
}
