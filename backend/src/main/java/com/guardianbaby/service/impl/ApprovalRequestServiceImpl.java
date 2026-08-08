package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.ApprovalRequestCreateDto;
import com.guardianbaby.dto.ApprovalRequestResponse;
import com.guardianbaby.entity.ApprovalRequest;
import com.guardianbaby.entity.ApprovalRequest.ApprovalStatus;
import com.guardianbaby.entity.ApprovalRequest.ApprovalType;
import com.guardianbaby.entity.User;
import com.guardianbaby.entity.User.UserType;
import com.guardianbaby.repository.ApprovalRequestRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.ApprovalRequestService;
import com.guardianbaby.service.AuditLogService;
import com.guardianbaby.service.AlertService;
import com.guardianbaby.service.TimeSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final TimeSettingsService timeSettingsService;
    private final AlertService alertService;

    @Override
    @Transactional
    public ApprovalRequestResponse submit(ApprovalRequestCreateDto dto) {
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (requester.getUserType() != UserType.PROTECTED) {
            throw new BusinessException("仅被保护用户可以发起请求");
        }

        ApprovalRequest ar = ApprovalRequest.builder()
                .approvalType(ApprovalType.valueOf(dto.getType().toUpperCase()))
                .description(dto.getDescription())
                .extraMinutes(dto.getExtraMinutes())
                .targetName(dto.getTargetName())
                .status(ApprovalStatus.PENDING)
                .requester(requester)
                .build();
        approvalRequestRepository.save(ar);
        auditLogService.log(dto.getRequesterId(), "SUBMIT_REQUEST", "提交请求: " + dto.getDescription(), "system");

        return ApprovalRequestResponse.fromEntity(ar);
    }

    @Override
    @Transactional
    public List<ApprovalRequestResponse> listPending(Long guardianId) {
        List<ApprovalRequest> all = approvalRequestRepository.findAllByOrderByCreatedAtDesc();
        LocalDateTime expiryThreshold = LocalDateTime.now().minusHours(24);
        return all.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .filter(a -> a.getRequester().getUserType() == UserType.PROTECTED)
                .map(a -> {
                    // Check expiry
                    if (a.getCreatedAt() != null && a.getCreatedAt().isBefore(expiryThreshold)) {
                        a.setStatus(ApprovalStatus.EXPIRED);
                        approvalRequestRepository.save(a);
                    }
                    return ApprovalRequestResponse.fromEntity(a);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> listMyRequests(Long requesterId) {
        return approvalRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(ApprovalRequestResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ApprovalRequestResponse approve(Long requestId, Long reviewerId, String message) {
        ApprovalRequest ar = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("请求不存在"));
        if (ar.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("该请求已被处理");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException("监护人不存在"));
        if (reviewer.getUserType() != UserType.GUARDIAN) {
            throw new BusinessException("仅监护人可以审批");
        }

        ar.setStatus(ApprovalStatus.APPROVED);
        ar.setReviewer(reviewer);
        ar.setResponseMessage(message);
        ar.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(ar);
        auditLogService.log(reviewerId, "APPROVE", "批准请求 #" + requestId, "system");

        // Execute the approved action
        if (ar.getApprovalType() == ApprovalType.TIME_EXTENSION && ar.getExtraMinutes() != null) {
            try {
                int extraMins = ar.getExtraMinutes();
                int extraHours = extraMins / 60;
                int extraRemainder = extraMins % 60;
                timeSettingsService.update(
                    ar.getRequester().getId(),
                    extraHours, extraRemainder,
                    null, null, null, null
                );
                alertService.create(
                    ar.getRequester().getId(),
                    "TIME_EXCEEDED", "LOW",
                    "时长已延长", ar.getRequester().getUsername() + " 的每日时长已延长 " + extraMins + " 分钟"
                );
            } catch (Exception ignored) { /* silent fail on time settings update */ }
        } else if (ar.getApprovalType() == ApprovalType.UNBLOCK) {
            alertService.create(
                ar.getRequester().getId(),
                "BLOCKED_CONTENT", "MEDIUM",
                "解除封锁已批准", ar.getTargetName() + " 已被监护人解除封锁"
            );
        }

        return ApprovalRequestResponse.fromEntity(ar);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse reject(Long requestId, Long reviewerId, String reason) {
        ApprovalRequest ar = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("请求不存在"));
        if (ar.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("该请求已被处理");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException("监护人不存在"));
        if (reviewer.getUserType() != UserType.GUARDIAN) {
            throw new BusinessException("仅监护人可以审批");
        }

        ar.setStatus(ApprovalStatus.REJECTED);
        ar.setReviewer(reviewer);
        ar.setResponseMessage(reason);
        ar.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(ar);

        auditLogService.log(reviewerId, "REJECT", "拒绝请求 #" + requestId + ": " + reason, "system");

        return ApprovalRequestResponse.fromEntity(ar);
    }

    @Override
    public long pendingCount() {
        return approvalRequestRepository.countByStatus(ApprovalStatus.PENDING);
    }
}
