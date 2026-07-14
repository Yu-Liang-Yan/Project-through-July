package com.guardianbaby.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private Long id;
    private String username;
    private String phone;
    private String userType;
    private String ageGroup;
    private String token;
    private String createdAt;
}
