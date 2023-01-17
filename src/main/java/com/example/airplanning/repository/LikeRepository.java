package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
}
