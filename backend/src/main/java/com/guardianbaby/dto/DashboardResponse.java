package com.guardianbaby.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Long todayUsage;  // 秒
    private Long weekUsage;   // 秒
    private Long deviceCount;
    private Long alertCount;
    private Long bindingCount;
    private Long onlineDeviceCount;
    private List<AlertSummary> recentAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSummary {
        private Long id;
        private String title;
        private String severity;
        private String type;
        private String protectedUserName;
        private String createdAt;
    }
}
