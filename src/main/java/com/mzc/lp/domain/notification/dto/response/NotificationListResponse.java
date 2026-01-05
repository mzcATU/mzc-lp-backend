package com.mzc.lp.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private long totalCount;
    private int page;
    private int pageSize;
    private int totalPages;
    private long unreadCount;

    public static NotificationListResponse of(
            Page<NotificationResponse> notificationPage,
            long unreadCount
    ) {
        return NotificationListResponse.builder()
                .notifications(notificationPage.getContent())
                .totalCount(notificationPage.getTotalElements())
                .page(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalPages(notificationPage.getTotalPages())
                .unreadCount(unreadCount)
                .build();
    }
}
