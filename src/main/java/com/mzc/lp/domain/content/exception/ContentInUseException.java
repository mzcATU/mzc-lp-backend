package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentInUseException extends BusinessException {

    public ContentInUseException(Long contentId) {
        super(ErrorCode.CONTENT_IN_USE,
                String.format("Content %d is in use by learning objects and cannot be modified", contentId));
    }
}
