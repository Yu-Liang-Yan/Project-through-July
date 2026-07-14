package com.guardianbaby.service;

import com.guardianbaby.dto.TimeSettingsResponse;

public interface TimeSettingsService {

    TimeSettingsResponse getByUserId(Long userId);

    TimeSettingsResponse update(Long userId,
                                Integer dailyHours, Integer dailyMinutes,
                                String startTime, String endTime,
                                Integer weeklyLimit, Integer monthlyLimit);
}
