package com.guardianbaby.repository;

import com.guardianbaby.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
