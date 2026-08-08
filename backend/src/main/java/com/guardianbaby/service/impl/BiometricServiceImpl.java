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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BiometricServiceImpl implements BiometricService {

    private final BiometricRecordRepository biometricRepository;
    private final UserRepository userRepository;

    // challenge cache: challengeId → {code, userId, expiresAt}
    private final Map<String, Map<String, Object>> challengeStore = new ConcurrentHashMap<>();

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
                .registeredAt(java.time.LocalDateTime.now())
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
    public Map<String, String> generateChallenge(Long userId) {
        // 检查用户是否有活跃的生物特征记录
        List<BiometricRecord> records = biometricRepository.findByUserId(userId);
        if (records.stream().noneMatch(r -> r.getStatus() == BiometricStatus.ACTIVE)) {
            throw new BusinessException("未注册生物特征，请先在「生物特征」页面注册");
        }

        // 生成 6 位验证码
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        String challengeId = UUID.randomUUID().toString().substring(0, 8);

        challengeStore.put(challengeId, Map.of(
            "code", code,
            "userId", userId,
            "expiresAt", Instant.now().plusSeconds(120) // 2分钟过期
        ));

        return Map.of("challengeId", challengeId, "code", code);
    }

    @Override
    public boolean verify(Long userId, String type) {
        List<BiometricRecord> records = biometricRepository.findByUserIdAndType(userId,
                BiometricType.valueOf(type.toUpperCase()));
        return records.stream().anyMatch(r -> r.getStatus() == BiometricStatus.ACTIVE);
    }

    @Override
    public boolean verifyChallenge(String challengeId, String code, Long userId) {
        // 清理过期 challenges
        challengeStore.entrySet().removeIf(e ->
                Instant.now().isAfter((Instant) e.getValue().get("expiresAt")));

        Map<String, Object> challenge = challengeStore.remove(challengeId);
        if (challenge == null) return false;

        Long storedUserId = (Long) challenge.get("userId");
        String storedCode = (String) challenge.get("code");

        if (!storedUserId.equals(userId)) return false;
        if (Instant.now().isAfter((Instant) challenge.get("expiresAt"))) return false;

        return storedCode.equals(code);
    }
}
