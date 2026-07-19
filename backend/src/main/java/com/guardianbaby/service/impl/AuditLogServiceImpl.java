package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.AuditLogResponse;
import com.guardianbaby.entity.AuditLog;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.AuditLogRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void log(Long userId, String action, String detail, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .detail(detail)
                .ip(ip)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
