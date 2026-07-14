package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.LoginRequest;
import com.guardianbaby.dto.LoginResponse;
import com.guardianbaby.dto.RegisterRequest;
import com.guardianbaby.dto.UserResponse;
import com.guardianbaby.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
