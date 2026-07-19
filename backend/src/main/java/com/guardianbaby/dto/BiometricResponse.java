package com.guardianbaby.dto;

import com.guardianbaby.entity.BiometricRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricResponse {
    private Long id;
    private Long userId;
    private String type;
    private Double confidenceThreshold;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastVerifiedAt;

    public static BiometricResponse fromEntity(BiometricRecord b) {
        return BiometricResponse.builder()
                .id(b.getId())
                .userId(b.getUser().getId())
                .type(b.getType().name())
                .confidenceThreshold(b.getConfidenceThreshold())
                .status(b.getStatus().name())
                .registeredAt(b.getRegisteredAt())
                .lastVerifiedAt(b.getLastVerifiedAt())
                .build();
    }
}
