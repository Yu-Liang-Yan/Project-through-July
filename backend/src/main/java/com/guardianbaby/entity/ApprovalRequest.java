package com.guardianbaby.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_approval_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 请求类型：TIME_EXTENSION(延长时间), UNBLOCK(解除禁止), ACCESS(访问特定内容) */
    @Column(name = "request_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalType approvalType;

    /** 请求详情描述 */
    @Column(length = 500)
    private String description;

    /** 额外时长（分钟），仅 TIME_EXTENSION 类型使用 */
    @Column(name = "extra_minutes")
    private Integer extraMinutes;

    /** 解除禁止的具体项目名称，仅 UNBLOCK 类型使用 */
    @Column(name = "target_name", length = 200)
    private String targetName;

    /** PENDING(待审批)  /  APPROVED(已同意)  /  REJECTED(已拒绝) */
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column(name = "response_message", length = 200)
    private String responseMessage;

    /** 发起请求的被保护用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** 处理请求的监护人 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum ApprovalType {
        TIME_EXTENSION, UNBLOCK, ACCESS
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ApprovalStatus.PENDING;
        }
    }
}
