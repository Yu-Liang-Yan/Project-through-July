package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.BiometricResponse;
import com.guardianbaby.entity.BiometricRecord;
import com.guardianbaby.entity.BiometricRecord.BiometricStatus;
import com.guardianbaby.entity.BiometricRecord.BiometricType;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.BiometricRecordRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BiometricServiceImpl implements BiometricService {

    private final BiometricRecordRepository biometricRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BiometricResponse register(Long userId, String type, Double confidenceThreshold) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        BiometricRecord record = BiometricRecord.builder()
                .user(user)
                .type(BiometricType.valueOf(type.toUpperCase()))
                .dataHash("hash_" + userId + "_" + type + "_" + System.currentTimeMillis())
                .confidenceThreshold(confidenceThreshold != null ? confidenceThreshold : 0.85)
                .status(BiometricStatus.ACTIVE)
                .registeredAt(LocalDateTime.now())
                .build();
        return BiometricResponse.fromEntity(biometricRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BiometricResponse> listByUser(Long userId) {
        return biometricRepository.findByUserId(userId).stream()
                .map(BiometricResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public BiometricResponse setStatus(Long recordId, boolean active) {
        BiometricRecord record = biometricRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("生物特征记录不存在"));
        record.setStatus(active ? BiometricStatus.ACTIVE : BiometricStatus.INACTIVE);
        return BiometricResponse.fromEntity(biometricRepository.save(record));
    }

    @Override
    public boolean verify(Long userId, String type) {
        // 模拟验证：检查用户是否有对应类型的活跃生物特征记录
        List<BiometricRecord> records = biometricRepository.findByUserIdAndType(userId, BiometricType.valueOf(type.toUpperCase()));
        return records.stream().anyMatch(r -> r.getStatus() == BiometricStatus.ACTIVE);
    }
}
