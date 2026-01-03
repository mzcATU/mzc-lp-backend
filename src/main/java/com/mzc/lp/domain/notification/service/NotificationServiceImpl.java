package com.mzc.lp.domain.notification.service;

import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.dto.response.NotificationListResponse;
import com.mzc.lp.domain.notification.dto.response.NotificationResponse;
import com.mzc.lp.domain.notification.dto.response.UnreadCountResponse;
import com.mzc.lp.domain.notification.entity.Notification;
import com.mzc.lp.domain.notification.exception.NotificationNotFoundException;
import com.mzc.lp.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationListResponse getNotifications(
            Long userId,
            NotificationType type,
            Boolean isRead,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Notification> notificationPage;

        if (type != null && isRead != null) {
            notificationPage = notificationRepository.findByUserIdAndTypeAndIsReadOrderByCreatedAtDesc(
                    userId, type, isRead, pageable);
        } else if (type != null) {
            notificationPage = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                    userId, type, pageable);
        } else if (isRead != null) {
            notificationPage = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
                    userId, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        long unreadCount = notificationRepository.countByUserIdAndIsRead(userId, false);

        return NotificationListResponse.of(responsePage, unreadCount);
    }

    @Override
    public NotificationResponse getNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        return NotificationResponse.from(notification);
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return UnreadCountResponse.of(count);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.markAsRead();
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId);
        log.debug("Marked {} notifications as read for user {}", updated, userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void deleteReadNotifications(Long userId) {
        int deleted = notificationRepository.deleteReadByUserId(userId);
        log.debug("Deleted {} read notifications for user {}", deleted, userId);
    }

    @Override
    @Transactional
    public void createNotification(
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
        // 중복 알림 방지: 같은 사용자, 같은 참조, 같은 액터면 생성하지 않음
        if (referenceId != null && actorId != null) {
            boolean exists = notificationRepository.existsByUserIdAndReferenceIdAndReferenceTypeAndActorId(
                    userId, referenceId, referenceType, actorId);
            if (exists) {
                log.debug("Duplicate notification skipped for user {} reference {} type {} actor {}",
                        userId, referenceId, referenceType, actorId);
                return;
            }
        }

        Notification notification = Notification.create(
                userId, type, title, message, link,
                referenceId, referenceType, actorId, actorName
        );
        notificationRepository.save(notification);
        log.debug("Created notification for user {}: {}", userId, title);
    }
}
