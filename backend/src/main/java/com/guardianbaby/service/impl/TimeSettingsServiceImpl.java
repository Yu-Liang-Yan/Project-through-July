package com.guardianbaby.service.impl;

import com.guardianbaby.dto.TimeSettingsResponse;
import com.guardianbaby.entity.TimeSettings;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.TimeSettingsRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.TimeSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeSettingsServiceImpl implements TimeSettingsService {

    private final TimeSettingsRepository timeSettingsRepository;
    private final UserRepository userRepository;

    @Override
    public TimeSettingsResponse getByUserId(Long userId) {
        TimeSettings ts = timeSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        return TimeSettingsResponse.fromEntity(ts);
    }

    @Override
    @Transactional
    public TimeSettingsResponse update(Long userId,
                                        Integer dailyHours, Integer dailyMinutes,
                                        String startTime, String endTime,
                                        Integer weeklyLimit, Integer monthlyLimit) {
        TimeSettings ts = timeSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    TimeSettings defaultTs = TimeSettings.builder()
                            .user(user)
                            .dailyHours(2).dailyMinutes(0)
                            .startTime("09:00").endTime("21:00")
                            .weeklyLimit(14).monthlyLimit(60)
                            .build();
                    return timeSettingsRepository.save(defaultTs);
                });

        ts.setDailyHours(dailyHours);
        ts.setDailyMinutes(dailyMinutes);
        ts.setStartTime(startTime);
        ts.setEndTime(endTime);
        ts.setWeeklyLimit(weeklyLimit);
        ts.setMonthlyLimit(monthlyLimit);
        timeSettingsRepository.save(ts);

        return TimeSettingsResponse.fromEntity(ts);
    }

    private TimeSettings createDefault(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        TimeSettings ts = TimeSettings.builder()
                .user(user)
                .dailyHours(2)
                .dailyMinutes(0)
                .startTime("09:00")
                .endTime("21:00")
                .weeklyLimit(14)
                .monthlyLimit(60)
                .build();
        return timeSettingsRepository.save(ts);
    }
}
