package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Page<Like> findAllByUser(User user, Pageable pageable);
    Boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    Boolean existsByPlannerIdAndUserId(Long plannerId, Long userId);
    Optional<Like> findByBoardIdAndUserId(Long boardId, Long userId);
    Optional<Like> findByPlannerIdAndUserId(Long plannerId, Long userId);
}
