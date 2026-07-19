package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.UserResponse;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") instanceof Number
                ? ((Number) body.get("userId")).longValue() : null;
        String phone = (String) body.get("phone");
        String ageGroup = (String) body.get("ageGroup");

        if (userId == null) {
            return ApiResponse.fail("缺少用户ID");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (phone != null && !phone.isBlank()) user.setPhone(phone);
        if (ageGroup != null && !ageGroup.isBlank()) user.setAgeGroup(ageGroup);
        userRepository.save(user);

        return ApiResponse.ok("资料更新成功", UserResponse.fromEntity(user));
    }
}
