package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentAccessDeniedException extends BusinessException {

    public ContentAccessDeniedException() {
        super(ErrorCode.UNAUTHORIZED_CONTENT_ACCESS);
    }

    public ContentAccessDeniedException(Long contentId, Long userId) {
        super(ErrorCode.UNAUTHORIZED_CONTENT_ACCESS,
                "User " + userId + " is not authorized to access content " + contentId);
    }
}