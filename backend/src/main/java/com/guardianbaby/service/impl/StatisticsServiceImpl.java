package com.guardianbaby.service.impl;

import com.guardianbaby.dto.DashboardResponse;
import com.guardianbaby.dto.DashboardResponse.AlertSummary;
import com.guardianbaby.dto.UsageRecordResponse;
import com.guardianbaby.entity.Alert;
import com.guardianbaby.entity.Device;
import com.guardianbaby.repository.AlertRepository;
import com.guardianbaby.repository.DeviceRepository;
import com.guardianbaby.repository.GuardianBindingRepository;
import com.guardianbaby.repository.UsageRecordRepository;
import com.guardianbaby.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UsageRecordRepository usageRecordRepository;
    private final DeviceRepository deviceRepository;
    private final GuardianBindingRepository bindingRepository;
    private final AlertRepository alertRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();

        Long todayUsage = usageRecordRepository.sumDurationSince(userId, todayStart);
        Long weekUsage = usageRecordRepository.sumDurationSince(userId, weekStart);
        Long monthUsage = usageRecordRepository.sumDurationSince(userId, monthStart);
        Long deviceCount = deviceRepository.countByOwnerId(userId);
        Long bindingCount = bindingRepository.countByGuardianId(userId);

        // 设备在线统计
        List<Device> devices = deviceRepository.findByOwnerId(userId);
        long onlineDeviceCount = devices.stream()
                .filter(d -> d.getStatus() == Device.DeviceStatus.ONLINE)
                .count();

        // 最近告警
        List<Alert> recentAlerts = alertRepository.findTop5ByGuardianIdOrderByCreatedAtDesc(userId);
        List<AlertSummary> alertSummaries = recentAlerts.stream()
                .map(a -> AlertSummary.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .severity(a.getSeverity().name())
                        .type(a.getType().name())
                        .protectedUserName(a.getProtectedUser().getUsername())
                        .createdAt(a.getCreatedAt().toString())
                        .build())
                .toList();

        long alertCount = alertRepository.countByGuardianIdAndStatus(userId, Alert.AlertStatus.NEW);

        return DashboardResponse.builder()
                .todayUsage(todayUsage)
                .weekUsage(weekUsage)
                .monthUsage(monthUsage)
                .deviceCount(deviceCount)
                .alertCount(alertCount)
                .bindingCount(bindingCount)
                .onlineDeviceCount(onlineDeviceCount)
                .recentAlerts(alertSummaries)
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
