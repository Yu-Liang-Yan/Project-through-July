package com.guardianbaby.controller;

import com.guardianbaby.dto.*;
import com.guardianbaby.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/biometrics")
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricService biometricService;

    @PostMapping
    public ApiResponse<BiometricResponse> register(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok("生物特征已注册", biometricService.register(
                Long.valueOf(body.get("userId").toString()),
                (String) body.get("type"),
                body.containsKey("confidenceThreshold") ? ((Number) body.get("confidenceThreshold")).doubleValue() : 0.85
        ));
    }

    @GetMapping
    public ApiResponse<List<BiometricResponse>> list(@RequestParam Long userId) {
        return ApiResponse.ok(biometricService.listByUser(userId));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<BiometricResponse> setStatus(@PathVariable Long id,
                                                     @RequestParam boolean active) {
        return ApiResponse.ok(biometricService.setStatus(id, active));
    }

    @PostMapping("/verify")
    public ApiResponse<Boolean> verify(@RequestBody Map<String, String> body) {
        boolean passed = biometricService.verify(
                Long.valueOf(body.get("userId")),
                body.get("type")
        );
        return ApiResponse.ok(passed ? "验证通过" : "验证失败", passed);
    }
}
