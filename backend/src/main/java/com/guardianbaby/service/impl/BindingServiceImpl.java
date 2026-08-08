package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.BindingResponse;
import com.guardianbaby.entity.GuardianBinding;
import com.guardianbaby.entity.GuardianBinding.BindingStatus;
import com.guardianbaby.entity.User;
import com.guardianbaby.entity.User.UserType;
import com.guardianbaby.repository.GuardianBindingRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.BindingService;
import com.guardianbaby.service.AuditLogService;
import com.guardianbaby.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BindingServiceImpl implements BindingService {

    private final GuardianBindingRepository bindingRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AlertService alertService;

    @Override
    @Transactional
    public BindingResponse bind(Long guardianId, Long protectedUserId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new BusinessException("监护人不存在"));
        if (guardian.getUserType() != UserType.GUARDIAN) {
            throw new BusinessException("该用户不是监护人");
        }

        User protectedUser = userRepository.findById(protectedUserId)
                .orElseThrow(() -> new BusinessException("被保护用户不存在"));
        if (protectedUser.getUserType() != UserType.PROTECTED) {
            throw new BusinessException("该用户不是被保护用户");
        }

        if (bindingRepository.findByGuardianIdAndProtectedUserId(guardianId, protectedUserId).isPresent()) {
            throw new BusinessException("该绑定关系已存在");
        }

        GuardianBinding binding = GuardianBinding.builder()
                .guardian(guardian)
                .protectedUser(protectedUser)
                .status(BindingStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        BindingResponse response = BindingResponse.fromEntity(bindingRepository.save(binding));
        auditLogService.log(guardianId, "BIND", "绑定被保护用户 #" + protectedUserId, "system");
        alertService.create(protectedUserId, "SYSTEM", "LOW",
            "新的监护关系", guardian.getUsername() + " 已成为你的监护人");
        return response;
    }

    @Override
    @Transactional
    public void unbind(Long bindingId) {
        if (!bindingRepository.existsById(bindingId)) {
            throw new BusinessException("绑定关系不存在");
        }
        bindingRepository.deleteById(bindingId);
        auditLogService.log(0L, "UNBIND", "解除绑定 #" + bindingId, "system");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BindingResponse> listProtectedUsers(Long guardianId) {
        return bindingRepository.findByGuardianId(guardianId).stream()
                .map(BindingResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BindingResponse> listGuardians(Long protectedUserId) {
        return bindingRepository.findByProtectedUserId(protectedUserId).stream()
                .map(BindingResponse::fromEntity)
                .toList();
    }
}
