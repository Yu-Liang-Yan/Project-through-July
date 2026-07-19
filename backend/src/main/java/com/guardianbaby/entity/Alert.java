package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", nullable = false)
    private User protectedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private User guardian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AlertSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AlertStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    public enum AlertType {
        UNAUTHORIZED_ACCESS, BLOCKED_CONTENT, TIME_EXCEEDED, SYSTEM
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlertStatus {
        NEW, READ, RESOLVED
    }
}
