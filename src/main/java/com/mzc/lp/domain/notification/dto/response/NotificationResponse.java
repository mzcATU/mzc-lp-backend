package com.mzc.lp.domain.notification.dto.response;

import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private String link;
    private Long referenceId;
    private String referenceType;
    private Long actorId;
    private String actorName;
    private Instant createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .link(notification.getLink())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .actorId(notification.getActorId())
                .actorName(notification.getActorName())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
