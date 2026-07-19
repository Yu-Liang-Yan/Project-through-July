package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_guardian_binding", uniqueConstraints = @UniqueConstraint(columnNames = {"guardian_id", "protected_user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardianBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", nullable = false)
    private User protectedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BindingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum BindingStatus {
        ACTIVE, INACTIVE
    }
}
