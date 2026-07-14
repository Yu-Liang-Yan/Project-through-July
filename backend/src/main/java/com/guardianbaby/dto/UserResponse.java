package com.guardianbaby.dto;

import com.guardianbaby.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String phone;
    private String userType;
    private String ageGroup;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .userType(user.getUserType().name())
                .ageGroup(user.getAgeGroup())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
