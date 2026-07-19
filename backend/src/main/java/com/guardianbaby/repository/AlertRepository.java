package com.guardianbaby.repository;

import com.guardianbaby.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByGuardianIdOrderByCreatedAtDesc(Long guardianId);

    List<Alert> findByProtectedUserIdOrderByCreatedAtDesc(Long protectedUserId);

    List<Alert> findByGuardianIdAndStatusOrderByCreatedAtDesc(Long guardianId, Alert.AlertStatus status);

    List<Alert> findByStatus(Alert.AlertStatus status);

    long countByGuardianIdAndStatus(Long guardianId, Alert.AlertStatus status);

    List<Alert> findTop5ByGuardianIdOrderByCreatedAtDesc(Long guardianId);
}
