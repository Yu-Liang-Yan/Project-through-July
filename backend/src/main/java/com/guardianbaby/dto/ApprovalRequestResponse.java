package com.guardianbaby.dto;

import com.guardianbaby.entity.ApprovalRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApprovalRequestResponse {

    private Long id;
    private String type;
    private String description;
    private Integer extraMinutes;
    private String targetName;
    private String status;
    private String responseMessage;
    private String requesterName;
    private Long requesterId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static ApprovalRequestResponse fromEntity(ApprovalRequest ar) {
        return ApprovalRequestResponse.builder()
                .id(ar.getId())
                .type(ar.getApprovalType().name())
                .description(ar.getDescription())
                .extraMinutes(ar.getExtraMinutes())
                .targetName(ar.getTargetName())
                .status(ar.getStatus().name())
                .responseMessage(ar.getResponseMessage())
                .requesterName(ar.getRequester().getUsername())
                .requesterId(ar.getRequester().getId())
                .createdAt(ar.getCreatedAt())
                .reviewedAt(ar.getReviewedAt())
                .build();
    }
}
