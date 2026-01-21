package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class EnrollmentPendingApprovalException extends BusinessException {

    public EnrollmentPendingApprovalException() {
        super(ErrorCode.ENROLLMENT_PENDING_APPROVAL);
    }

    public EnrollmentPendingApprovalException(Long enrollmentId) {
        super(ErrorCode.ENROLLMENT_PENDING_APPROVAL,
              "Enrollment " + enrollmentId + " is pending approval");
    }
}
