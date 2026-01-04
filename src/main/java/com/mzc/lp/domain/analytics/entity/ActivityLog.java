package com.mzc.lp.domain.analytics.entity;

import com.mzc.lp.common.entity.BaseEntity;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 활동 로그 엔티티
 */
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_log_tenant", columnList = "tenantId"),
    @Index(name = "idx_activity_log_user", columnList = "userId"),
    @Index(name = "idx_activity_log_type", columnList = "activityType"),
    @Index(name = "idx_activity_log_created", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ActivityLog extends BaseEntity {

    @Column(nullable = true)
    private Long tenantId;

    @Column(nullable = true)
    private Long userId;

    @Column(length = 100)
    private String userName;

    @Column(length = 100)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String targetType;

    @Column
    private Long targetId;

    @Column(length = 255)
    private String targetName;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    /**
     * 로그 생성 팩토리 메서드
     */
    public static ActivityLog create(
            Long tenantId,
            Long userId,
            String userName,
            String userEmail,
            ActivityType activityType,
            String description,
            String targetType,
            Long targetId,
            String targetName,
            String ipAddress,
            String userAgent
    ) {
        return ActivityLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .activityType(activityType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }
}
