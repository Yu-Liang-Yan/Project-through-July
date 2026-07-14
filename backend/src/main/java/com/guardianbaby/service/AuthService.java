package com.guardianbaby.service;

import com.guardianbaby.dto.LoginRequest;
import com.guardianbaby.dto.LoginResponse;
import com.guardianbaby.dto.RegisterRequest;
import com.guardianbaby.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);
}
