package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotAnnouncementAuthorException extends BusinessException {

    public NotAnnouncementAuthorException() {
        super(ErrorCode.CM_NOT_ANNOUNCEMENT_AUTHOR);
    }

    public NotAnnouncementAuthorException(Long announcementId) {
        super(ErrorCode.CM_NOT_ANNOUNCEMENT_AUTHOR, "Not authorized to modify announcement: " + announcementId);
    }
}
