package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.TimeSettingsResponse;
import com.guardianbaby.service.TimeSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/time-settings")
@RequiredArgsConstructor
public class TimeSettingsController {

    private final TimeSettingsService timeSettingsService;

    @GetMapping
    public ApiResponse<TimeSettingsResponse> get(@RequestParam Long userId) {
        return ApiResponse.ok(timeSettingsService.getByUserId(userId));
    }

    @PutMapping
    public ApiResponse<TimeSettingsResponse> update(@RequestParam Long userId,
                                                     @RequestBody Map<String, Object> body) {
        TimeSettingsResponse result = timeSettingsService.update(
                userId,
                getInt(body, "dailyHours", 2),
                getInt(body, "dailyMinutes", 0),
                getString(body, "startTime", "09:00"),
                getString(body, "endTime", "21:00"),
                getInt(body, "weeklyLimit", 14),
                getInt(body, "monthlyLimit", 60)
        );
        return ApiResponse.ok("时间设置已更新", result);
    }

    private Integer getInt(Map<String, Object> body, String key, Integer def) {
        Object val = body.get(key);
        return val instanceof Number ? ((Number) val).intValue() : def;
    }

    private String getString(Map<String, Object> body, String key, String def) {
        Object val = body.get(key);
        return val instanceof String ? (String) val : def;
    }
}
