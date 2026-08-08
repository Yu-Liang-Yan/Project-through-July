package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.AlertResponse;
import com.guardianbaby.entity.Alert;
import com.guardianbaby.entity.Alert.AlertStatus;
import com.guardianbaby.entity.Alert.AlertSeverity;
import com.guardianbaby.entity.Alert.AlertType;
import com.guardianbaby.entity.User;
import com.guardianbaby.entity.GuardianBinding;
import com.guardianbaby.repository.AlertRepository;
import com.guardianbaby.repository.GuardianBindingRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.AlertService;
import com.guardianbaby.websocket.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final GuardianBindingRepository bindingRepository;
    private final WebSocketPushService pushService;

    @Override
    @Transactional
    public AlertResponse create(Long protectedUserId, String type, String severity, String title, String message) {
        User protectedUser = userRepository.findById(protectedUserId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 查找关联的监护人
        List<GuardianBinding> bindings = bindingRepository.findByProtectedUserId(protectedUserId);
        User guardian = bindings.isEmpty() ? null : bindings.get(0).getGuardian();

        Alert alert = Alert.builder()
                .protectedUser(protectedUser)
                .guardian(guardian)
                .type(AlertType.valueOf(type))
                .severity(AlertSeverity.valueOf(severity))
                .title(title)
                .message(message)
                .status(AlertStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();
        AlertResponse response = AlertResponse.fromEntity(alertRepository.save(alert));

        // WebSocket push to guardian
        if (guardian != null) {
            pushService.push(guardian.getId(), "ALERT", response);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> listForGuardian(Long guardianId) {
        return alertRepository.findByGuardianIdOrderByCreatedAtDesc(guardianId).stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> listForProtectedUser(Long protectedUserId) {
        return alertRepository.findByProtectedUserIdOrderByCreatedAtDesc(protectedUserId).stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public AlertResponse markRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException("告警不存在"));
        alert.setStatus(AlertStatus.READ);
        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public AlertResponse resolve(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException("告警不存在"));
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Override
    public long unreadCount(Long guardianId) {
        return alertRepository.countByGuardianIdAndStatus(guardianId, AlertStatus.NEW);
    }
}
