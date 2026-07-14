package com.guardianbaby.dto;

import com.guardianbaby.entity.Device;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeviceResponse {

    private Long id;
    private String name;
    private String type;
    private String status;
    private LocalDateTime lastActive;
    private LocalDateTime registeredAt;

    public static DeviceResponse fromEntity(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getDeviceType().name().toLowerCase())
                .status(device.getStatus().name().toLowerCase())
                .lastActive(device.getLastActive())
                .registeredAt(device.getRegisteredAt())
                .build();
    }
}
