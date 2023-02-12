package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PointRepository extends JpaRepository<Point, Long> {

    Page<Point> findAllByUser(User user, Pageable pageable);



}