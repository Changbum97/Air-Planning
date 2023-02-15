package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 글 제목
    private String content;     // 글 내용
    private String image;       // 이미지 URL
    private Long regionId;      // 자신있는 지역

    @ColumnDefault(value = "0")
    private Integer views;      // 조회수

    @Enumerated(EnumType.STRING)
    private Category category;  // 카테고리 (자유게시판, 등업게시판, 포트폴리오 게시판)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER, orphanRemoval = true)   //게시글 삭제시 댓글도 함께 삭제
    @OrderBy("id asc") // 댓글 정렬
    private List<Comment> comments;

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    private List<Like> likes;

    
    // 수정
    public void modify(String title, String content){
        this.title = title;
        this.content = content;
    }

    // 수정 + 파일 수정
    public void modify(String title, String content, String image){
        this.title = title;
        this.content = content;
        this.image = image;
    }

    // 조회수 증가
    public void addViews() {
        this.views ++;
    }

}
