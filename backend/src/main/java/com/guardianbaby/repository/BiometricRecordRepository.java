package com.guardianbaby.repository;

import com.guardianbaby.entity.BiometricRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BiometricRecordRepository extends JpaRepository<BiometricRecord, Long> {

    List<BiometricRecord> findByUserId(Long userId);

    List<BiometricRecord> findByUserIdAndType(Long userId, BiometricRecord.BiometricType type);

    List<BiometricRecord> findByUserIdAndStatus(Long userId, BiometricRecord.BiometricStatus status);
}
