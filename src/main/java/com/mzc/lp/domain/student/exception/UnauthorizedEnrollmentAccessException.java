package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class UnauthorizedEnrollmentAccessException extends BusinessException {

    public UnauthorizedEnrollmentAccessException() {
        super(ErrorCode.UNAUTHORIZED_ENROLLMENT_ACCESS);
    }

    public UnauthorizedEnrollmentAccessException(Long enrollmentId, Long userId) {
        super(ErrorCode.UNAUTHORIZED_ENROLLMENT_ACCESS,
                "User " + userId + " is not authorized to access enrollment " + enrollmentId);
    }
}
