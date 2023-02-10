package com.example.airplanning.domain.dto;

import com.example.airplanning.domain.entity.Alarm;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AlarmResponse {

    private Long id;
    private String targetUrl;       // 알람 클릭 시 이동할 URL
    private String alarmMessage;
    private String title;
    private String type;

    public static AlarmResponse of(Alarm alarm) {
        String title = alarm.getTitle();
        if (title.length() > 9) {
            title = title.substring(0,8);
            title = "["+title+"...]";
        } else {
            title = "["+title+"]";
        }
        System.out.println(title);
        return new AlarmResponse(alarm.getId(), alarm.getTargetUrl(), alarm.getAlarmType().getMessage(), title, alarm.getAlarmType().toString());
    }
}
