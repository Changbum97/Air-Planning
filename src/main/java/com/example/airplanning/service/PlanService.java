package com.example.airplanning.service;

import com.example.airplanning.domain.dto.plan.PlanCreateRequest;
import com.example.airplanning.domain.dto.plan.PlanDto;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public PlanDto create(PlanCreateRequest planCreateRequest, String userName){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.save(planCreateRequest.toEntity(user));

        return PlanDto.of(plan);

    }

}
