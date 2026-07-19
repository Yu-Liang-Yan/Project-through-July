package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.LoginRequest;
import com.guardianbaby.dto.LoginResponse;
import com.guardianbaby.dto.RegisterRequest;
import com.guardianbaby.dto.UserResponse;
import com.guardianbaby.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ApiResponse.ok("登录成功", result);
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ApiResponse.ok("注册成功", user);
    }

    /** 双重验证：修改敏感设置前需再次确认密码 */
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyPassword(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") instanceof Number
                ? ((Number) body.get("userId")).longValue() : null;
        String password = (String) body.get("password");

        if (userId == null || password == null) {
            return ApiResponse.fail("请提供 userId 和 password");
        }

        boolean valid = authService.verifyPassword(userId, password);
        if (valid) {
            return ApiResponse.ok("验证通过", true);
        } else {
            return ApiResponse.fail("密码错误，验证失败");
        }
    }
}
