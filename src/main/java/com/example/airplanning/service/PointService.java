package com.example.airplanning.service;

import com.example.airplanning.domain.dto.point.PointCancelResponse;
import com.example.airplanning.domain.dto.point.PointRequest;
import com.example.airplanning.domain.dto.point.PointResponse;
import com.example.airplanning.domain.dto.point.PointVo;
import com.example.airplanning.domain.entity.Point;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.PointRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    //해당 유저가 보유한 포인트 확인
    @Transactional(readOnly = true)
    public Integer getUserPoint(String userName){
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        return user.getPoint();
    }

    //포인트 충전
    @Transactional
    public PointResponse chargePoint(String userName, PointVo pointVo) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));
        log.info(userName);

        System.out.println("유저 포인트 : "+user.getPoint());

        Integer amount = pointVo.getAmount();
        System.out.println("포인트 결제 금액 : "+amount);

        Point point = Point.createPoint(user, pointVo.getAmount(), pointVo.getImp_uid());
        System.out.println("create 포인트 금액 : "+point.getAmount());

        user.updatePoint(plusPoint(user, amount));
        System.out.println("유저 포인트 : "+user.getPoint());

        userRepository.save(user);
        System.out.println(user.getPoint());

        Point savePoint = pointRepository.save(point);
        return PointResponse.of(savePoint);

    }

    // 결제 진행 시 포인트 +
    public Integer plusPoint(User user, Integer amount){
        return user.getPoint() + amount;
    }

    // 결제 취소 시 포인트 -
    public Integer minusPoint(User user, Integer amount){
        return user.getPoint() - amount;
    }


    // 포인트 결제 내역 상세 조회
    @Transactional(readOnly = true)
    public PointResponse getOrderDetail(String userName, Long pointId) {
        User user = getUser(userName);
        Point point = getPoint(pointId);
        validateUser(user, point);
        return PointResponse.of(point);
    }

    //해당 결제 내역이 있는지 확인
    public Point getPoint(Long pointId){
        return pointRepository.findById(pointId)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    //해당 유저가 존재하는 지 확인
    public User getUser(String userName) {
        return userRepository.findById(Long.parseLong(userName)).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
    }

    // 포인트 결제 유저와 로그인 유저가 동일한 지 확인.
    public static void validateUser(User user, Point point) {
        if (point.getUser().getId() != user.getId()) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    @Transactional
    public PointCancelResponse cancelOrder(String userName, Long pointId) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_PERMISSION));

        Point point = pointRepository.findById(pointId)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateUser(user, point);

        // 주문 시 회원 포인트 정보 수정
        user.updatePoint(minusPoint(user, point.getAmount()));

        // 주문 취소로 변경
        point.paymentStatusChange(point.getPointStatus());

        // 업데이트한 유저 저장
        userRepository.save(user);
        // 주문 저장
        pointRepository.save(point);
        return PointCancelResponse.of(point);
    }



// view service

    @Transactional
    public PointResponse charge(String userName, PointVo pointVo){

        User user = getUser(userName);
        log.info("user={}", user);

        //주문 생성
        Point point = Point.createPoint(user, pointVo.getAmount(), pointVo.getImp_uid());

        //포인트 정보 수정
        Integer plusPoint = plusPoint(user, pointVo.getAmount());
        log.info("결제 진행 후 포인트={}", plusPoint);

        //유저에 저장
        user.updatePoint(plusPoint);
        userRepository.save(user);

        Point savePoint = pointRepository.save(point);
        return PointResponse.of(savePoint);
    }
}
