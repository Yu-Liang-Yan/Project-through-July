package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_biometric_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometricRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BiometricType type;

    @Column(length = 128)
    private String dataHash;

    @Column(nullable = false)
    private Double confidenceThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BiometricStatus status;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime lastVerifiedAt;

    public enum BiometricType {
        FACE, FINGERPRINT, VOICEPRINT, KEYSTROKE
    }

    public enum BiometricStatus {
        ACTIVE, INACTIVE
    }
}
