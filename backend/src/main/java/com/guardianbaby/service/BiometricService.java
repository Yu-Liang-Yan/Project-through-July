package com.guardianbaby.service;

import com.guardianbaby.dto.BiometricResponse;
import java.util.List;
import java.util.Map;

public interface BiometricService {
    BiometricResponse register(Long userId, String type, Double confidenceThreshold);
    List<BiometricResponse> listByUser(Long userId);
    BiometricResponse setStatus(Long recordId, boolean active);
    boolean verify(Long userId, String type);
    Map<String, String> generateChallenge(Long userId);
    boolean verifyChallenge(String challengeId, String code, Long userId);
}
