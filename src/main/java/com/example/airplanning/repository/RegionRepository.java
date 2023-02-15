package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByRegion1AndRegion2(String region1, String region2);
}
