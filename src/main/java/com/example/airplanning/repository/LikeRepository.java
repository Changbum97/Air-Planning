package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Like;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Page<Like> findAllByUser(User user, Pageable pageable);
}
