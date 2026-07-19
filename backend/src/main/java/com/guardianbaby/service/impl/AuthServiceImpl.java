package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.config.JwtUtil;
import com.guardianbaby.dto.LoginRequest;
import com.guardianbaby.dto.LoginResponse;
import com.guardianbaby.dto.RegisterRequest;
import com.guardianbaby.dto.UserResponse;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.AuditLogService;
import com.guardianbaby.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        auditLogService.log(user.getId(), "LOGIN", "用户登录系统", request.getUsername());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .userType(user.getUserType().name())
                .ageGroup(user.getAgeGroup())
                .token(token)
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .userType(User.UserType.valueOf(request.getUserType().toUpperCase()))
                .ageGroup(request.getAgeGroup())
                .build();

        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Override
    public boolean verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log(userId, "changePassword", "用户修改密码", "127.0.0.1");
    }
}
