package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Page<Plan> findAllByUser(User user, Pageable pageable);
    Page<Plan> findAllByPlannerAndPlanTypeNot(Planner planner, PlanType type, Pageable pageable);

    Page<Plan> findAllByPlannerAndPlanType(Planner planner, PlanType type, Pageable pageable);
}
