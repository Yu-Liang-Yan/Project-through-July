package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_content_filter_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentFilterRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FilterCategory category;

    @Column(nullable = false, length = 500)
    private String pattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private FilterAction action;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum FilterCategory {
        KEYWORD, WEBSITE, DOMAIN, APP
    }

    public enum FilterAction {
        BLOCK, WARN, LOG
    }
}
