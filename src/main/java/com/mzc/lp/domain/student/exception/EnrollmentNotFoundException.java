package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class EnrollmentNotFoundException extends BusinessException {

    public EnrollmentNotFoundException() {
        super(ErrorCode.ENROLLMENT_NOT_FOUND);
    }

    public EnrollmentNotFoundException(Long enrollmentId) {
        super(ErrorCode.ENROLLMENT_NOT_FOUND, "Enrollment not found with id: " + enrollmentId);
    }
}
