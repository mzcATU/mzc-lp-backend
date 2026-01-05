package com.mzc.lp.domain.notification.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.notification.constant.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인 알림 엔티티
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notification_user", columnList = "tenant_id, user_id"),
        @Index(name = "idx_notification_user_unread", columnList = "tenant_id, user_id, is_read"),
        @Index(name = "idx_notification_created", columnList = "tenant_id, user_id, created_at DESC")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    // ===== 정적 팩토리 메서드 =====
    public static Notification create(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String link,
            Long referenceId,
            String referenceType,
            Long actorId,
            String actorName
    ) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.type = type;
        notification.title = title;
        notification.message = message;
        notification.link = link;
        notification.referenceId = referenceId;
        notification.referenceType = referenceType;
        notification.actorId = actorId;
        notification.actorName = actorName;
        notification.isRead = false;
        return notification;
    }

    // ===== 비즈니스 메서드 =====
    public void markAsRead() {
        this.isRead = true;
    }
}
