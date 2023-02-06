package com.example.airplanning.domain.entity;

import com.example.airplanning.domain.dto.plan.PlanUpdateResponse;
import com.example.airplanning.domain.enum_class.PlanType;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id")
    private Planner planner;

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }

    public void refusePlan(PlanType planType){
        if (planType.equals(PlanType.REFUSE)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        this.planType = PlanType.REFUSE;
    }

    public void acceptPlan(PlanType planType){
        if (planType.equals(PlanType.ACCEPT)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        this.planType = PlanType.ACCEPT;
    }

}
