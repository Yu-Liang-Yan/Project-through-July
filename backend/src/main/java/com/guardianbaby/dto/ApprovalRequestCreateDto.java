package com.guardianbaby.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestCreateDto {

    private Long requesterId;
    private String type;
    private String description;
    private Integer extraMinutes;
    private String targetName;
}
