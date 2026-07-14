package com.guardianbaby.repository;

import com.guardianbaby.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByOwnerId(Long ownerId);

    long countByOwnerId(Long ownerId);
}
