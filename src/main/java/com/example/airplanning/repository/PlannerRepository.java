package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannerRepository extends JpaRepository<Planner, Long> {
}
