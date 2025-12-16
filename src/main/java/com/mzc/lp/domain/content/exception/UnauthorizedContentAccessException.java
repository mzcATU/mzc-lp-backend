package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class UnauthorizedContentAccessException extends BusinessException {

    public UnauthorizedContentAccessException() {
        super(ErrorCode.UNAUTHORIZED_CONTENT_ACCESS);
    }

    public UnauthorizedContentAccessException(Long contentId) {
        super(ErrorCode.UNAUTHORIZED_CONTENT_ACCESS, "Not authorized to access content: " + contentId);
    }
}
