package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class EnrollmentPeriodClosedException extends BusinessException {

    public EnrollmentPeriodClosedException() {
        super(ErrorCode.ENROLLMENT_PERIOD_CLOSED);
    }

    public EnrollmentPeriodClosedException(Long courseTimeId) {
        super(ErrorCode.ENROLLMENT_PERIOD_CLOSED,
              "Enrollment period is closed for CourseTime: " + courseTimeId);
    }
}
