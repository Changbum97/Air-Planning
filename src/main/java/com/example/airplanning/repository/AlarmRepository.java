package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Alarm;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Page<Alarm> findAllByUser(User user, Pageable pageable);
}
