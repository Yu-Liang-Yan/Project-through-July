package com.guardianbaby.repository;

import com.guardianbaby.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u JOIN GuardianBinding b ON u.id = b.protectedUser.id WHERE b.guardian.id = :guardianId")
    List<User> findProtectedByGuardianId(Long guardianId);
}
