package com.guardianbaby.service;

import com.guardianbaby.dto.BiometricResponse;
import java.util.List;

public interface BiometricService {
    BiometricResponse register(Long userId, String type, Double confidenceThreshold);
    List<BiometricResponse> listByUser(Long userId);
    BiometricResponse setStatus(Long recordId, boolean active);
    boolean verify(Long userId, String type);
}
