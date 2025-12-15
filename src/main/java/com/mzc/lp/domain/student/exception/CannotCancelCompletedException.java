package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CannotCancelCompletedException extends BusinessException {

    public CannotCancelCompletedException() {
        super(ErrorCode.CANNOT_CANCEL_COMPLETED);
    }

    public CannotCancelCompletedException(Long enrollmentId) {
        super(ErrorCode.CANNOT_CANCEL_COMPLETED,
              "Cannot cancel completed enrollment: " + enrollmentId);
    }
}
