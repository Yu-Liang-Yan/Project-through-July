package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/heartbeat")
    public ApiResponse<Void> heartbeat(@RequestParam Long userId) {
        agentService.heartbeat(userId);
        return ApiResponse.ok("心跳已接收", null);
    }

    @PostMapping("/usage")
    public ApiResponse<Void> reportUsage(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") instanceof Number
                ? ((Number) body.get("userId")).longValue() : null;
        String deviceName = (String) body.get("deviceName");
        String appName = (String) body.get("appName");
        int durationSeconds = body.get("durationSeconds") instanceof Number
                ? ((Number) body.get("durationSeconds")).intValue() : 0;

        if (userId == null || appName == null || durationSeconds <= 0) {
            return ApiResponse.fail("参数不完整");
        }

        agentService.reportUsage(userId,
                deviceName != null ? deviceName : "未知设备",
                appName, durationSeconds);
        return ApiResponse.ok("ok", null);
    }
}
