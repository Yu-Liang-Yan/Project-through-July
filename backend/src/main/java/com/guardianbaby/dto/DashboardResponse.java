package com.guardianbaby.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private Long todayUsage;  // 秒
    private Long weekUsage;   // 秒
    private Long deviceCount;
    private Long alertCount;
}
