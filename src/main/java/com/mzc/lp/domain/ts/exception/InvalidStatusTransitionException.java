package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;

public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException() {
        super(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    public InvalidStatusTransitionException(CourseTimeStatus from, CourseTimeStatus to) {
        super(ErrorCode.INVALID_STATUS_TRANSITION,
                String.format("Invalid status transition from %s to %s", from, to));
    }
}
