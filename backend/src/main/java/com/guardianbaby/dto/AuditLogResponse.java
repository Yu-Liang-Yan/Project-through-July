package com.guardianbaby.dto;

import com.guardianbaby.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .username(log.getUser().getUsername())
                .action(log.getAction())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
