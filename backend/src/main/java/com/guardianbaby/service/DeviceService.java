package com.guardianbaby.service;

import com.guardianbaby.dto.DeviceResponse;

import java.util.List;

public interface DeviceService {

    List<DeviceResponse> listDevices(Long userId);

    DeviceResponse addDevice(Long userId, String name, String type);

    void removeDevice(Long deviceId, Long userId);

    DeviceResponse lockDevice(Long deviceId, Long userId);

    DeviceResponse unlockDevice(Long deviceId, Long userId);
}
