package com.guardianbaby.service.impl;

import com.guardianbaby.dto.DashboardResponse;
import com.guardianbaby.dto.UsageRecordResponse;
import com.guardianbaby.repository.DeviceRepository;
import com.guardianbaby.repository.UsageRecordRepository;
import com.guardianbaby.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UsageRecordRepository usageRecordRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public DashboardResponse getDashboard(Long userId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();

        Long todayUsage = usageRecordRepository.sumDurationSince(userId, todayStart);
        Long weekUsage = usageRecordRepository.sumDurationSince(userId, weekStart);
        Long deviceCount = deviceRepository.countByOwnerId(userId);

        return DashboardResponse.builder()
                .todayUsage(todayUsage)
                .weekUsage(weekUsage)
                .deviceCount(deviceCount)
                .alertCount(0L)
                .build();
    }

    @Override
    public List<UsageRecordResponse> getUsageRecords(Long userId, String period) {
        LocalDateTime since = switch (period) {
            case "week" -> LocalDate.now().minusDays(7).atStartOfDay();
            case "month" -> LocalDate.now().minusDays(30).atStartOfDay();
            default -> LocalDate.now().atStartOfDay();
        };

        return usageRecordRepository.findByUserIdSince(userId, since).stream()
                .map(UsageRecordResponse::fromEntity)
                .toList();
    }
}
