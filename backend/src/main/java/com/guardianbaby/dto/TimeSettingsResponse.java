package com.guardianbaby.dto;

import com.guardianbaby.entity.TimeSettings;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSettingsResponse {

    private Long id;
    private Integer dailyHours;
    private Integer dailyMinutes;
    private String startTime;
    private String endTime;
    private Integer weeklyLimit;
    private Integer monthlyLimit;

    public static TimeSettingsResponse fromEntity(TimeSettings ts) {
        return TimeSettingsResponse.builder()
                .id(ts.getId())
                .dailyHours(ts.getDailyHours())
                .dailyMinutes(ts.getDailyMinutes())
                .startTime(ts.getStartTime())
                .endTime(ts.getEndTime())
                .weeklyLimit(ts.getWeeklyLimit())
                .monthlyLimit(ts.getMonthlyLimit())
                .build();
    }
}
