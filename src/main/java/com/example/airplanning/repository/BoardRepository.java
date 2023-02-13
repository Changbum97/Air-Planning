package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Page<Board> findAllByUser(User user, Pageable pageable);

    Page<Board> findAllByCategory(Category category, Pageable pageable);

    Page<Board> findByCategoryAndTitleContains(Category category, String title, Pageable pageable);
    Page<Board> findByCategoryAndUserNicknameContains(Category category, String nickname, Pageable pageable);
}
