package com.mzc.lp.domain.notification.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotificationTemplateNotFoundException extends BusinessException {
    public NotificationTemplateNotFoundException(Long id) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, "알림 템플릿을 찾을 수 없습니다. ID: " + id);
    }
}
