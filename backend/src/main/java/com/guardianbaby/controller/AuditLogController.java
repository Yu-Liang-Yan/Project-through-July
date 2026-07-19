package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.AuditLogResponse;
import com.guardianbaby.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/user")
    public ApiResponse<List<AuditLogResponse>> listByUser(@RequestParam Long userId) {
        return ApiResponse.ok(auditLogService.listByUser(userId));
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> listAll() {
        return ApiResponse.ok(auditLogService.listAll());
    }
}
