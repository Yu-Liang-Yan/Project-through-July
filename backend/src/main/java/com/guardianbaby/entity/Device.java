package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "device_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public enum DeviceType {
        PHONE, TABLET, COMPUTER, WATCH, READER
    }

    public enum DeviceStatus {
        ONLINE, OFFLINE, LOCKED
    }

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
