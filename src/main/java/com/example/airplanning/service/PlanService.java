package com.example.airplanning.service;

import com.example.airplanning.domain.dto.plan.*;
import com.example.airplanning.domain.entity.Plan;
import com.example.airplanning.domain.entity.Planner;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PlanRepository;
import com.example.airplanning.repository.PlannerRepository;
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

    private final PlannerRepository plannerRepository;

    private final AlarmService alarmService;

    public PlanDto create(PlanCreateRequest planCreateRequest, String userName){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_PERMISSION));

        Planner planner = plannerRepository.findById(planCreateRequest.getPlannerId())
                .orElseThrow(()->new AppException(ErrorCode.PLANNER_NOT_FOUNDED));

        Plan plan = planRepository.save(planCreateRequest.toEntity(user, planner));

        alarmService.send(planner.getUser(), AlarmType.REQUEST_PLAN_ALARM, "/plans/"+plan.getId(), plan.getTitle());

        return PlanDto.of(plan);

    }

    public PlanDto detail(Long id, String userName){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (plan.getUser().getId() != user.getId() && user.getRole() != UserRole.PLANNER && user.getRole() != UserRole.ADMIN){
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

    public Page<PlanListResponse> list(Pageable pageable){
        Page<Plan> plans = planRepository.findAll(pageable);

        return PlanListResponse.toDtoList(plans);
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

        alarmService.send(plan.getUser(), AlarmType.REFUSED_PLAN_ALARM, "/plans/"+plan.getId(), plan.getTitle());

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

        alarmService.send(plan.getUser(), AlarmType.ACCEPTED_PLAN_ALARM, "/plans/"+plan.getId(), plan.getTitle());

        return PlanResponse.of(plan);
    }

    public PlanPaymentRequest getInfo(String userName, Long planId){
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        System.out.println(user.getPoint());
        System.out.println(plan.getPlanner().getAmount());

        return PlanPaymentRequest.of(plan);
    }

    @Transactional
    public PlanPaymentRequest usedPoint(String userName, Long planId){

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(()-> new AppException(ErrorCode.BOARD_NOT_FOUND));

        System.out.println(user.getPoint());
        System.out.println(plan.getPlanner().getAmount());

        // 유저 보유 포인트보다 플래너의 가격이 더 비쌀 때 오류 처리
        if (user.getPoint() < plan.getPlanner().getAmount()){
            throw new AppException(ErrorCode.INVALID_LACK_OF_POINT);
        }

        System.out.println(user.getUserName());
        System.out.println(plan.getPlanner().getUser().getUserName());

        // 포인트 사용 시 회원 포인트 정보 수정
        user.updatePoint(PointService.minusPoint(user, plan.getPlanner().getAmount()));

        // 유저가 포인트 결제 시 플래너에게 포인트 +
        plan.getPlanner().getUser().plusPoint(plan.getPlanner().getAmount());

        userRepository.save(user);

        // 플랜 타입 결제 완료로 변경
        plan.completePlan(plan.getPlanType());

        planRepository.save(plan);

        return PlanPaymentRequest.of(plan);
    }


}
