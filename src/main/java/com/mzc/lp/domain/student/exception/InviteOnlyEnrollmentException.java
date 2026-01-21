package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InviteOnlyEnrollmentException extends BusinessException {

    public InviteOnlyEnrollmentException() {
        super(ErrorCode.INVITE_ONLY_ENROLLMENT);
    }

    public InviteOnlyEnrollmentException(Long courseTimeId) {
        super(ErrorCode.INVITE_ONLY_ENROLLMENT,
              "CourseTime " + courseTimeId + " only accepts invited users");
    }
}
