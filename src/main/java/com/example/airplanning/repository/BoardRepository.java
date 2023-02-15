package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Page<Board> findAllByUser(User user, Pageable pageable);

    Page<Board> findAllByCategory(Category category, Pageable pageable);

    Page<Board> findByCategoryAndTitleContains(Category category, String title, Pageable pageable);
    Page<Board> findByCategoryAndUserNicknameContains(Category category, String nickname, Pageable pageable);

    Page<Board> findByCategoryAndTitleContainsAndRegionId(Category category, String title, Long regionId, Pageable pageable);

    Page<Board> findByCategoryAndTitleContainsAndRegionRegion1(Category category, String title, String region1, Pageable pageable);

    Page<Board> findByCategoryAndUserNicknameContainsAndRegionId(Category category, String nickname, Long regionId, Pageable pageable);
    Page<Board> findByCategoryAndUserNicknameContainsAndRegionRegion1(Category category, String nickname, String region1, Pageable pageable);
}
