package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentNotFoundException extends BusinessException {

    public ContentNotFoundException() {
        super(ErrorCode.CONTENT_NOT_FOUND);
    }

    public ContentNotFoundException(Long contentId) {
        super(ErrorCode.CONTENT_NOT_FOUND, "Content not found with id: " + contentId);
    }
}
