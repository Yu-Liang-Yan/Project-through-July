package com.guardianbaby.repository;

import com.guardianbaby.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
