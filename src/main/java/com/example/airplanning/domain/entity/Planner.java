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
    private String description;     // 자기 소개
    private Integer amount;         // 플랜 가격

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(mappedBy = "planner", cascade = CascadeType.REMOVE)
    private List<Like> likes;

    @OneToMany(mappedBy = "planner", cascade = CascadeType.REMOVE)
    private List<Plan> plans;
}
