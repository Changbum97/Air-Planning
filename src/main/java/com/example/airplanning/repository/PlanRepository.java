package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
