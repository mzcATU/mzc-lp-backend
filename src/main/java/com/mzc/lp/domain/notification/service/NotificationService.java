package com.mzc.lp.domain.notification.service;

import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.dto.response.NotificationListResponse;
import com.mzc.lp.domain.notification.dto.response.NotificationResponse;
import com.mzc.lp.domain.notification.dto.response.UnreadCountResponse;

public interface NotificationService {

    /**
     * 알림 목록 조회
     */
    NotificationListResponse getNotifications(
            Long userId,
            NotificationType type,
            Boolean isRead,
            int page,
            int pageSize
    );

    /**
     * 알림 상세 조회
     */
    NotificationResponse getNotification(Long userId, Long notificationId);

    /**
     * 읽지 않은 알림 개수 조회
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * 알림 읽음 처리
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 모든 알림 읽음 처리
     */
    void markAllAsRead(Long userId);

    /**
     * 알림 삭제
     */
    void deleteNotification(Long userId, Long notificationId);

    /**
     * 읽은 알림 전체 삭제
     */
    void deleteReadNotifications(Long userId);

    /**
     * 알림 생성 (내부 사용)
     */
    void createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String link,
            Long referenceId,
            String referenceType,
            Long actorId,
            String actorName
    );
}
