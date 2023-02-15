package com.example.airplanning.service;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.dto.planner.PlannerEditRequest;
import com.example.airplanning.domain.dto.planner.PlannerPlanResponse;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.PlannerRepository;
import com.example.airplanning.repository.RegionRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    private final PlanRepository planRepository;

    public PlannerDetailResponse findById(Long plannerId) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNER_NOT_FOUNDED));
        return PlannerDetailResponse.of(planner);
    }

    public PlannerDetailResponse findByUser(String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Planner planner = plannerRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        return PlannerDetailResponse.of(planner);
    }

    @Transactional
    public Long edit(Long plannerId, PlannerEditRequest plannerEditRequest) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNER_NOT_FOUNDED));
        Region region = regionRepository.findById(plannerEditRequest.getRegionId())
                        .orElseThrow(() -> new AppException(ErrorCode.REGION_NOT_FOUND));
        planner.edit(plannerEditRequest.getDescription(), region);

        return plannerId;
    }

    public Page<PlannerPlanResponse> getWaitingPlan (Long plannerId, Pageable pageable) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(()->new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        Page<Plan> plans = planRepository.findAllByPlannerAndPlanType(planner, PlanType.WAITING, pageable);
        return plans.map(plan -> PlannerPlanResponse.of(plan));
    }

    public Page<PlannerPlanResponse> getAcceptedPlan (Long plannerId, Pageable pageable) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(()->new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        Page<Plan> plans = planRepository.findAllByPlannerAndPlanType(planner, PlanType.ACCEPT, pageable);
        return plans.map(plan -> PlannerPlanResponse.of(plan));
    }

    public Page<PlannerPlanResponse> getRefusedPlan (Long plannerId, Pageable pageable) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(()->new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        Page<Plan> plans = planRepository.findAllByPlannerAndPlanType(planner, PlanType.REFUSE, pageable);
        return plans.map(plan -> PlannerPlanResponse.of(plan));
    }

    public Page<PlannerPlanResponse> getCompletedPlan (Long plannerId, Pageable pageable) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(()->new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        Page<Plan> plans = planRepository.findAllByPlannerAndPlanType(planner, PlanType.COMPLETE, pageable);
        return plans.map(plan -> PlannerPlanResponse.of(plan));
    }
}
