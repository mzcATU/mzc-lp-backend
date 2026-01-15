package com.mzc.lp.domain.notification.constant;

/**
 * 알림 트리거 타입
 * 특정 이벤트 발생 시 자동으로 알림을 생성하는 트리거 종류
 */
public enum NotificationTrigger {
    // 인증 관련
    WELCOME("회원가입 환영", NotificationCategory.AUTH),

    // 알림 관련
    ENROLLMENT_COMPLETE("수강신청 완료", NotificationCategory.NOTIFICATION),
    COURSE_COMPLETE("과정 완료 축하", NotificationCategory.NOTIFICATION);

    private final String displayName;
    private final NotificationCategory category;

    NotificationTrigger(String displayName, NotificationCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NotificationCategory getCategory() {
        return category;
    }
}
