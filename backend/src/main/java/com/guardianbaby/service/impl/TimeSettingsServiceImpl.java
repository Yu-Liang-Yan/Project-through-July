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

    private static final String DEFAULT_START_TIME = "09:00";
    private static final String DEFAULT_END_TIME = "21:00";

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
                .orElseGet(() -> createDefault(userId));

        ts.setDailyHours(dailyHours);
        ts.setDailyMinutes(dailyMinutes);
        ts.setStartTime(startTime);
        ts.setEndTime(endTime);
        ts.setWeeklyLimit(weeklyLimit);
        ts.setMonthlyLimit(monthlyLimit);
        timeSettingsRepository.save(ts);

        return TimeSettingsResponse.fromEntity(ts);
    }

    /**
     * 根据被保护对象的年龄段返回不同的默认时长限制：
     * 6岁以下:    0.5h/天  /  3h/周   /  10h/月  /  08:00-19:00
     * 6-12岁:     1h/天    /  7h/周   /  30h/月  /  09:00-20:00
     * 12-15岁:    2h/天    / 14h/周   /  60h/月  /  09:00-21:00
     * 15-18岁:    3h/天    / 21h/周   /  90h/月  /  09:00-22:00
     * 监护人（无年龄分层）: 2h/天 / 14h/周 / 60h/月
     */
    private TimeSettings createDefault(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        int dailyHours = 2;
        int dailyMinutes = 0;
        int weeklyLimit = 14;
        int monthlyLimit = 60;
        String startTime = DEFAULT_START_TIME;
        String endTime = DEFAULT_END_TIME;

        if (user.getUserType() == User.UserType.PROTECTED && user.getAgeGroup() != null) {
            switch (user.getAgeGroup()) {
                case "0-6":
                    dailyHours = 0; dailyMinutes = 30;
                    weeklyLimit = 3; monthlyLimit = 10;
                    startTime = "08:00"; endTime = "19:00";
                    break;
                case "6-12":
                    dailyHours = 1; dailyMinutes = 0;
                    weeklyLimit = 7; monthlyLimit = 30;
                    startTime = "09:00"; endTime = "20:00";
                    break;
                case "12-15":
                    dailyHours = 2; dailyMinutes = 0;
                    weeklyLimit = 14; monthlyLimit = 60;
                    break;
                case "15-18":
                    dailyHours = 3; dailyMinutes = 0;
                    weeklyLimit = 21; monthlyLimit = 90;
                    startTime = "09:00"; endTime = "22:00";
                    break;
            }
        }

        TimeSettings ts = TimeSettings.builder()
                .user(user)
                .dailyHours(dailyHours).dailyMinutes(dailyMinutes)
                .startTime(startTime).endTime(endTime)
                .weeklyLimit(weeklyLimit).monthlyLimit(monthlyLimit)
                .build();
        return timeSettingsRepository.save(ts);
    }
}
