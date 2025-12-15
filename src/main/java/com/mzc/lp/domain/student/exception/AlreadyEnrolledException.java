package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AlreadyEnrolledException extends BusinessException {

    public AlreadyEnrolledException() {
        super(ErrorCode.ALREADY_ENROLLED);
    }

    public AlreadyEnrolledException(Long userId, Long courseTimeId) {
        super(ErrorCode.ALREADY_ENROLLED,
              "User " + userId + " is already enrolled in CourseTime " + courseTimeId);
    }
}
