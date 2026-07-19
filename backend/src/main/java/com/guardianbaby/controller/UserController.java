package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.UserResponse;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return ApiResponse.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/protected")
    public ApiResponse<List<UserResponse>> listProtected(@RequestParam Long guardianId) {
        return ApiResponse.ok(
                userRepository.findProtectedByGuardianId(guardianId).stream()
                        .map(UserResponse::fromEntity)
                        .toList()
        );
    }
}
