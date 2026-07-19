package com.guardianbaby.dto;

import com.guardianbaby.entity.GuardianBinding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingResponse {
    private Long id;
    private Long guardianId;
    private String guardianName;
    private Long protectedUserId;
    private String protectedUserName;
    private String protectedAgeGroup;
    private String status;
    private LocalDateTime createdAt;

    public static BindingResponse fromEntity(GuardianBinding b) {
        return BindingResponse.builder()
                .id(b.getId())
                .guardianId(b.getGuardian().getId())
                .guardianName(b.getGuardian().getUsername())
                .protectedUserId(b.getProtectedUser().getId())
                .protectedUserName(b.getProtectedUser().getUsername())
                .protectedAgeGroup(b.getProtectedUser().getAgeGroup())
                .status(b.getStatus().name())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
