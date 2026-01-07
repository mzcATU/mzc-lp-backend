package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AnnouncementNotFoundException extends BusinessException {

    public AnnouncementNotFoundException() {
        super(ErrorCode.CM_ANNOUNCEMENT_NOT_FOUND);
    }

    public AnnouncementNotFoundException(Long announcementId) {
        super(ErrorCode.CM_ANNOUNCEMENT_NOT_FOUND, "Announcement not found: " + announcementId);
    }
}
