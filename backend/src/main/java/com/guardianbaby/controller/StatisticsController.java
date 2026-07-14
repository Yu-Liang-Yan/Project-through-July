package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.DashboardResponse;
import com.guardianbaby.dto.UsageRecordResponse;
import com.guardianbaby.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard(@RequestParam Long userId) {
        return ApiResponse.ok(statisticsService.getDashboard(userId));
    }

    @GetMapping("/usage")
    public ApiResponse<List<UsageRecordResponse>> usage(@RequestParam Long userId,
                                                         @RequestParam(defaultValue = "day") String period) {
        return ApiResponse.ok(statisticsService.getUsageRecords(userId, period));
    }
}
