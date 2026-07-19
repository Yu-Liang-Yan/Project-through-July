package com.guardianbaby.repository;

import com.guardianbaby.entity.GuardianBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuardianBindingRepository extends JpaRepository<GuardianBinding, Long> {

    List<GuardianBinding> findByGuardianId(Long guardianId);

    List<GuardianBinding> findByProtectedUserId(Long protectedUserId);

    Optional<GuardianBinding> findByGuardianIdAndProtectedUserId(Long guardianId, Long protectedUserId);

    long countByGuardianIdAndStatus(Long guardianId, GuardianBinding.BindingStatus status);

    long countByGuardianId(Long guardianId);
}
