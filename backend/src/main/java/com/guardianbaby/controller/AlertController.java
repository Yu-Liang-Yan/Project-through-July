package com.guardianbaby.controller;

import com.guardianbaby.dto.*;
import com.guardianbaby.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ApiResponse<AlertResponse> create(@RequestBody Map<String, String> body) {
        return ApiResponse.ok("告警已创建", alertService.create(
                Long.valueOf(body.get("protectedUserId")),
                body.get("type"),
                body.get("severity"),
                body.get("title"),
                body.get("message")
        ));
    }

    @GetMapping("/guardian")
    public ApiResponse<List<AlertResponse>> listForGuardian(@RequestParam Long guardianId) {
        return ApiResponse.ok(alertService.listForGuardian(guardianId));
    }

    @GetMapping("/protected-user")
    public ApiResponse<List<AlertResponse>> listForProtectedUser(@RequestParam Long protectedUserId) {
        return ApiResponse.ok(alertService.listForProtectedUser(protectedUserId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<AlertResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok("已标记为已读", alertService.markRead(id));
    }

    @PutMapping("/{id}/resolve")
    public ApiResponse<AlertResponse> resolve(@PathVariable Long id) {
        return ApiResponse.ok("告警已解决", alertService.resolve(id));
    }

    @GetMapping("/count")
    public ApiResponse<Long> unreadCount(@RequestParam Long guardianId) {
        return ApiResponse.ok(alertService.unreadCount(guardianId));
    }
}
