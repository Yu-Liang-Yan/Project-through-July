package com.guardianbaby.dto;

import com.guardianbaby.entity.UsageRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsageRecordResponse {

    private Long id;
    private String device;
    private String app;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;

    public static UsageRecordResponse fromEntity(UsageRecord record) {
        return UsageRecordResponse.builder()
                .id(record.getId())
                .device(record.getDevice())
                .app(record.getApp())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .duration(record.getDuration())
                .build();
    }
}
