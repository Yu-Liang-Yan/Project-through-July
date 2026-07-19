package com.guardianbaby.service;

import com.guardianbaby.dto.LoginRequest;
import com.guardianbaby.dto.LoginResponse;
import com.guardianbaby.dto.RegisterRequest;
import com.guardianbaby.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    /** 双重验证：再次验证密码（模拟密码+生物特征确认） */
    boolean verifyPassword(Long userId, String password);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
