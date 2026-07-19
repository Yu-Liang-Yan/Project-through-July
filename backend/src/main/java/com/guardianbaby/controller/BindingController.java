package com.guardianbaby.controller;

import com.guardianbaby.dto.*;
import com.guardianbaby.service.BindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bindings")
@RequiredArgsConstructor
public class BindingController {

    private final BindingService bindingService;

    @PostMapping
    public ApiResponse<BindingResponse> bind(@RequestParam Long guardianId,
                                              @RequestParam Long protectedUserId) {
        return ApiResponse.ok("绑定成功", bindingService.bind(guardianId, protectedUserId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> unbind(@PathVariable Long id) {
        bindingService.unbind(id);
        return ApiResponse.ok("解绑成功", null);
    }

    @GetMapping("/protected-users")
    public ApiResponse<List<BindingResponse>> listProtectedUsers(@RequestParam Long guardianId) {
        return ApiResponse.ok(bindingService.listProtectedUsers(guardianId));
    }

    @GetMapping("/guardians")
    public ApiResponse<List<BindingResponse>> listGuardians(@RequestParam Long protectedUserId) {
        return ApiResponse.ok(bindingService.listGuardians(protectedUserId));
    }
}
