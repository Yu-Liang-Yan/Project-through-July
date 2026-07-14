package com.guardianbaby.repository;

import com.guardianbaby.entity.TimeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeSettingsRepository extends JpaRepository<TimeSettings, Long> {

    Optional<TimeSettings> findByUserId(Long userId);
}
