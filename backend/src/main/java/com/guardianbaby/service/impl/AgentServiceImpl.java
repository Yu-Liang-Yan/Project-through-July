package com.guardianbaby.service.impl;

import com.guardianbaby.entity.Device;
import com.guardianbaby.entity.UsageRecord;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.DeviceRepository;
import com.guardianbaby.repository.UsageRecordRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final DeviceRepository deviceRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void heartbeat(Long userId) {
        // Update all devices owned by this user to ONLINE and bump lastActive
        deviceRepository.findByOwnerId(userId).forEach(device -> {
            device.setStatus(Device.DeviceStatus.ONLINE);
            device.setLastActive(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }

    @Override
    @Transactional
    public void reportUsage(Long userId, String deviceName, String appName, int durationSeconds) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        LocalDateTime now = LocalDateTime.now();
        UsageRecord record = UsageRecord.builder()
                .user(user)
                .device(deviceName)
                .app(appName)
                .startTime(now.minusSeconds(durationSeconds))
                .endTime(now)
                .duration(durationSeconds)
                .build();
        usageRecordRepository.save(record);
    }
}
