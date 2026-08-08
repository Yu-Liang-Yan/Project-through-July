package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.DeviceResponse;
import com.guardianbaby.service.DeviceService;
import com.guardianbaby.service.impl.DiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DiscoveryService discoveryService;

    // 临时：从请求参数获取 userId，第二轮改 JWT 后从 token 中提取
    @GetMapping
    public ApiResponse<List<DeviceResponse>> list(@RequestParam Long userId) {
        return ApiResponse.ok(deviceService.listDevices(userId));
    }

    @PostMapping("/discover")
    public ApiResponse<List<Map<String, Object>>> discover() {
        List<Map<String, Object>> devices = discoveryService.discover();
        return ApiResponse.ok("发现 " + devices.size() + " 个设备", devices);
    }

    @PostMapping
    public ApiResponse<DeviceResponse> add(@RequestParam Long userId,
                                           @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String type = body.get("type");
        DeviceResponse device = deviceService.addDevice(userId, name, type);
        return ApiResponse.ok("设备添加成功", device);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id,
                                    @RequestParam Long userId) {
        deviceService.removeDevice(id, userId);
        return ApiResponse.ok("设备已移除", null);
    }

    @PutMapping("/{id}/lock")
    public ApiResponse<DeviceResponse> lock(@PathVariable Long id,
                                            @RequestParam Long userId) {
        return ApiResponse.ok("设备已锁定", deviceService.lockDevice(id, userId));
    }

    @PutMapping("/{id}/unlock")
    public ApiResponse<DeviceResponse> unlock(@PathVariable Long id,
                                              @RequestParam Long userId) {
        return ApiResponse.ok("设备已解锁", deviceService.unlockDevice(id, userId));
    }
}
