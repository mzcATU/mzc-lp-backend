package com.mzc.lp.domain.notification.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;

public class DuplicateNotificationTemplateException extends BusinessException {
    public DuplicateNotificationTemplateException(NotificationTrigger triggerType) {
        super(ErrorCode.INVALID_INPUT_VALUE, "이미 동일한 트리거 타입의 템플릿이 존재합니다: " + triggerType.getDisplayName());
    }
}
