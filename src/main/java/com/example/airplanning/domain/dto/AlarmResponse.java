package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.Alarm;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AlarmResponse {

    private String targetUrl;       // 알람 클릭 시 이동할 URL

    private String alarmMessage;

    public static AlarmResponse of(Alarm alarm) {
        return new AlarmResponse(alarm.getTargetUrl(), alarm.getAlarmType().getMessage());
    }
}
