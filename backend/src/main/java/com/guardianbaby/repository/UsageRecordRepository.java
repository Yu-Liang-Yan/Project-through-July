package com.guardianbaby.repository;

import com.guardianbaby.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByUserId(Long userId);

    @Query("SELECT u FROM UsageRecord u WHERE u.user.id = :userId AND u.startTime >= :since")
    List<UsageRecord> findByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(u.duration), 0) FROM UsageRecord u WHERE u.user.id = :userId AND u.startTime >= :since")
    Long sumDurationSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UsageRecord u WHERE u.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
