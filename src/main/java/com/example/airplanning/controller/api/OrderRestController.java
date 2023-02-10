package com.example.airplanning.controller.api;

import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.point.*;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.UserRepository;
import com.example.airplanning.service.PaymentService;
import com.example.airplanning.service.PointService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class OrderRestController {

    //결제 동작 controller

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PointService pointService;

    @ApiOperation(value = "카드 결제 -> 완료 -> 검증")
    @PostMapping(value = "/order/payment/complete")
    public ResponseEntity<Response<?>> paymentComplete(@RequestBody PointVo pointVo, Principal principal) throws IOException {

        String userName = principal.getName();
        User user = userRepository.findByUserName(userName).get();
        log.info(userName);

        // 1. 아임포트 API 키와 SECRET키로 토큰을 생성
        String token = paymentService.getToken();
        log.info("token = {}", token);

        // 2. 토큰으로 결제 완료된 결제정보(결제 완료된 금액) 가져옴
        int amount = paymentService.paymentInfo(pointVo.getImp_uid(), token);
        System.out.println("결제 완료된 금액 = " + amount);
        log.info(pointVo.getImp_uid());

        try {

            //검증 단계
            // 3. 결제 누르기 전 계산되어야 할 가격 가져오기(실제 계산 금액 가져오기)
            long orderPriceCheck = pointVo.getAmount();
            log.info("DB상 실제 계산 금액 = " + orderPriceCheck);

            // 4. 결제 완료된 금액과 DB상 계산되어야 할 금액이 다를경우 결제 취소
            if (orderPriceCheck != amount) {
                paymentService.paymentCancel(token, pointVo.getImp_uid(), amount, "결제 금액 오류");
                return ResponseEntity.badRequest().body(Response.error(new AppException(ErrorCode.INVALID_ORDER_TOTAL_POINT)));
            }

            PointResponse pointResponse = pointService.chargePoint(principal.getName(), pointVo);

            return ResponseEntity.ok().body(Response.success(pointResponse));

        }catch (Exception e){
            paymentService.paymentCancel(token, pointVo.getImp_uid(), amount, "결제 에러");
            return ResponseEntity.badRequest().body(Response.error(new AppException(ErrorCode.INVALID_ORDER)));
        }

    }

    @ApiOperation(value = "결제 취소")
    @PostMapping(value = "/order/payment/cancel")
    public ResponseEntity<Response<?>> paymentCancel(@RequestBody PointCancelVo pointCancelVo, Principal principal) throws IOException {
        log.info("principal.getName()={}", principal.getName());

        // 1. 아임포트 API 키와 SECRET키로 토큰을 생성
        String token = paymentService.getToken();
        log.info("token = {}", token);

        String imp_uid = pointCancelVo.getImp_uid();
        System.out.println("imp_uid = " + imp_uid);

        // ** 주문 취소 **
        // Imp_uid가 null이 아니면 아임포트 결제임 -> 아임포트 결제취소 api 호출해야 함
        if (pointCancelVo.getImp_uid() != null) {
            // 2. 토큰으로 결제 완료된 결제정보(결제 완료된 금액) 가져옴
            int amount = paymentService.paymentInfo(pointCancelVo.getImp_uid(), token);
            log.info("amount={}", amount);
            paymentService.paymentCancel(token, pointCancelVo.getImp_uid(), amount, "결제 에러");
        }
        PointCancelResponse pointCancelResponse = pointService.cancelOrder(principal.getName(), pointCancelVo.getPointId());
        return ResponseEntity.ok().body(Response.success(pointCancelResponse));
    }

}
