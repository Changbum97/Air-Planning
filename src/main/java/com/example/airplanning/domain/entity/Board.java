package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 글 제목
    private String content;     // 글 내용
    private String image;       // 이미지 URL

    @Enumerated(EnumType.ORDINAL)
    private Category category;  // 카테고리 (자유게시판, 등업게시판, 포트폴리오 게시판)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "board")
    private List<Comment> comments;

    @OneToMany(mappedBy = "board")
    private List<Like> likes;
}
