package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentDownloadNotAllowedException extends BusinessException {

    public ContentDownloadNotAllowedException() {
        super(ErrorCode.CONTENT_DOWNLOAD_NOT_ALLOWED);
    }

    public ContentDownloadNotAllowedException(Long contentId) {
        super(ErrorCode.CONTENT_DOWNLOAD_NOT_ALLOWED, "Download is not allowed for content: " + contentId);
    }
}
