package com.example.airplanning.service;

import com.example.airplanning.domain.dto.plan.*;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    public PlanDto detail(Long id, String userName){

        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        if (plan.getUser().getUserName() != user.getUserName() && user.getRole() != UserRole.PLANNER && user.getRole() != UserRole.ADMIN){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        return PlanDto.of(plan);
    }

    @Transactional
    public Long update(Long id, PlanUpdateRequest updateRequest, String userName){

        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));

        if (plan.getUser().getUserName() != user.getUserName()){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        plan.update(updateRequest.getTitle(), updateRequest.getContent());
        planRepository.save(plan);

        return id;
    }

    public Plan planview(Long id){
        return planRepository.findById(id).get();
    }

    @Transactional
    public Long delete(Long id, String userName){

        Plan plan = planRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));

        if (plan.getUser().getUserName() != user.getUserName()){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        planRepository.deleteById(id);
        return id;
    }

    public Page<PlanDto> list(Pageable pageable){
        Page<Plan> planPage = planRepository.findAll(pageable);
        Page<PlanDto> planDtos = PlanDto.planDto(planPage);

        return planDtos;
    }

    public PlanResponse refusePlan(Long id, String userName){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (user.getRole() == UserRole.USER){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        plan.refusePlan(plan.getPlanType());

        planRepository.save(plan);

        return PlanResponse.of(plan);
    }

    public PlanResponse acceptPlan(Long id, String userName){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (user.getRole() == UserRole.USER){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        plan.acceptPlan(plan.getPlanType());

        planRepository.save(plan);

        return PlanResponse.of(plan);
    }


}
