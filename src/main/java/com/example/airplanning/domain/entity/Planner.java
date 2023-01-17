package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.enum_class.Theme;
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
public class Planner extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer reviewCount;    // 리뷰 개수
    private Integer starSum;        // 별점 총 합
    private String country;         // 자신있는 국가
    private String region;          // 자신있는 지역
    private String description;     // 자기 소개

    @Enumerated(EnumType.STRING)
    private Theme theme1;            // 테마 (선택지 중 선택, 3개까지 가능, 최소 1개)
    @Enumerated(EnumType.STRING)
    private Theme theme2;
    @Enumerated(EnumType.STRING)
    private Theme theme3;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "planner")
    private List<Like> likes;
}
