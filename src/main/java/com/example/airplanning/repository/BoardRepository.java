package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Page<Board> findAllByUser(User user, Pageable pageable);
}
