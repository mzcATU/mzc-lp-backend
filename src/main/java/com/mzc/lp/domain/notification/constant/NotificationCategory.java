package com.mzc.lp.domain.notification.constant;

/**
 * 알림 템플릿 카테고리
 */
public enum NotificationCategory {
    AUTH("인증"),
    NOTIFICATION("알림"),
    MARKETING("마케팅"),
    SYSTEM("시스템");

    private final String displayName;

    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
