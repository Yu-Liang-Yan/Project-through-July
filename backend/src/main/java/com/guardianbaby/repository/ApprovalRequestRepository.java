package com.guardianbaby.repository;

import com.guardianbaby.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalRequest.ApprovalStatus status);

    long countByStatus(ApprovalRequest.ApprovalStatus status);

    List<ApprovalRequest> findAllByOrderByCreatedAtDesc();
}
