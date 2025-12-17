package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentVersionNotFoundException extends BusinessException {

    public ContentVersionNotFoundException(Long contentId, Integer version) {
        super(ErrorCode.CONTENT_VERSION_NOT_FOUND,
                String.format("Version %d not found for content %d", version, contentId));
    }
}
