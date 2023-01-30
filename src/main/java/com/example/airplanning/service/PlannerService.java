package com.example.airplanning.service;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final PlannerRepository plannerRepository;

    public PlannerDetailResponse findById(Long plannerId) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNER_NOT_FOUNDED));
        return PlannerDetailResponse.of(planner);
    }
}
