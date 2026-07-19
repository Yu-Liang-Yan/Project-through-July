package com.guardianbaby.dto;

import com.guardianbaby.entity.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private Long protectedUserId;
    private String protectedUserName;
    private Long guardianId;
    private String type;
    private String severity;
    private String title;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertResponse fromEntity(Alert a) {
        AlertResponse r = AlertResponse.builder()
                .id(a.getId())
                .protectedUserId(a.getProtectedUser().getId())
                .protectedUserName(a.getProtectedUser().getUsername())
                .type(a.getType().name())
                .severity(a.getSeverity().name())
                .title(a.getTitle())
                .message(a.getMessage())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
        if (a.getGuardian() != null) {
            r.setGuardianId(a.getGuardian().getId());
        }
        return r;
    }
}
