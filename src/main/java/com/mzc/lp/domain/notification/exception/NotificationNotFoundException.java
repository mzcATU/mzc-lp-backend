package com.mzc.lp.domain.notification.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotificationNotFoundException extends BusinessException {
    public NotificationNotFoundException(Long notificationId) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, "Notification not found: " + notificationId);
    }
}
