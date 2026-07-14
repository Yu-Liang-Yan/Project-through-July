package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_time_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_hours", nullable = false)
    private Integer dailyHours;

    @Column(name = "daily_minutes", nullable = false)
    private Integer dailyMinutes;

    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime;

    @Column(name = "weekly_limit", nullable = false)
    private Integer weeklyLimit;

    @Column(name = "monthly_limit", nullable = false)
    private Integer monthlyLimit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
