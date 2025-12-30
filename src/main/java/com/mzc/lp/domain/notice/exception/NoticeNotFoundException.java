package com.mzc.lp.domain.notice.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NoticeNotFoundException extends BusinessException {

    public NoticeNotFoundException(Long noticeId) {
        super(ErrorCode.NOTICE_NOT_FOUND, "Notice not found: " + noticeId);
    }
}
