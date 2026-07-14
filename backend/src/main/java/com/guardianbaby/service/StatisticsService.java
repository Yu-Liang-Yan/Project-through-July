package com.guardianbaby.service;

import com.guardianbaby.dto.DashboardResponse;
import com.guardianbaby.dto.UsageRecordResponse;

import java.util.List;

public interface StatisticsService {

    DashboardResponse getDashboard(Long userId);

    List<UsageRecordResponse> getUsageRecords(Long userId, String period);
}
