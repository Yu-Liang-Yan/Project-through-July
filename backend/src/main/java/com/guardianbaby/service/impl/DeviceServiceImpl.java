package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.DeviceResponse;
import com.guardianbaby.entity.Device;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.DeviceRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Override
    public List<DeviceResponse> listDevices(Long userId) {
        return deviceRepository.findByOwnerId(userId).stream()
                .map(DeviceResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public DeviceResponse addDevice(Long userId, String name, String type) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Device device = Device.builder()
                .name(name)
                .deviceType(Device.DeviceType.valueOf(type.toUpperCase()))
                .status(Device.DeviceStatus.ONLINE)
                .lastActive(LocalDateTime.now())
                .owner(owner)
                .build();

        deviceRepository.save(device);
        return DeviceResponse.fromEntity(device);
    }

    @Override
    @Transactional
    public void removeDevice(Long deviceId, Long userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("设备不存在"));

        if (!device.getOwner().getId().equals(userId)) {
            throw new BusinessException("无权操作该设备");
        }

        deviceRepository.delete(device);
    }
}
