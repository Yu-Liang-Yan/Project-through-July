package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.DashboardResponse;
import com.guardianbaby.entity.Alert;
import com.guardianbaby.entity.AuditLog;
import com.guardianbaby.entity.UsageRecord;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.*;
import com.guardianbaby.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final AlertRepository alertRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/export")
    public ApiResponse<Map<String, Object>> exportData(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ApiResponse.fail("用户不存在");

        DashboardResponse dashboard = statisticsService.getDashboard(userId);
        List<UsageRecord> records = usageRecordRepository.findByUserId(userId);
        List<Alert> alerts = alertRepository.findByGuardianIdOrderByCreatedAtDesc(userId);
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exportTime", java.time.LocalDateTime.now().toString());
        data.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "phone", user.getPhone(),
                "userType", user.getUserType().name(),
                "ageGroup", user.getAgeGroup()
        ));
        data.put("stats", dashboard);
        data.put("usageRecords", records.stream().limit(50).map(r -> Map.of(
                "id", r.getId(), "device", r.getDevice(), "app", r.getApp(),
                "duration", r.getDuration(), "startTime", r.getStartTime().toString()
        )).collect(Collectors.toList()));
        data.put("alerts", alerts.stream().limit(20).map(a -> Map.of(
                "id", a.getId(), "title", a.getTitle(), "severity", a.getSeverity().name()
        )).collect(Collectors.toList()));
        data.put("auditLogs", logs.stream().limit(20).map(l -> Map.of(
                "id", l.getId(), "action", l.getAction(), "detail", l.getDetail()
        )).collect(Collectors.toList()));

        return ApiResponse.ok("导出成功", data);
    }

    @Transactional
    @DeleteMapping("/clear")
    public ApiResponse<?> clearData(@RequestParam Long userId) {
        int usageDeleted = usageRecordRepository.deleteByUserId(userId);
        int auditDeleted = auditLogRepository.deleteByUserId(userId);

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("usageRecordsDeleted", usageDeleted);
        result.put("auditLogsDeleted", auditDeleted);

        return ApiResponse.ok("数据已清除", result);
    }
}
