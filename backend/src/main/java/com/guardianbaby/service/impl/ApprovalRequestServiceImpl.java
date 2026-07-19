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

        return ApprovalRequestResponse.fromEntity(ar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> listPending(Long guardianId) {
        List<ApprovalRequest> all = approvalRequestRepository.findAllByOrderByCreatedAtDesc();
        return all.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .filter(a -> a.getRequester().getUserType() == UserType.PROTECTED)
                .map(ApprovalRequestResponse::fromEntity)
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

        return ApprovalRequestResponse.fromEntity(ar);
    }

    @Override
    public long pendingCount() {
        return approvalRequestRepository.countByStatus(ApprovalStatus.PENDING);
    }
}
