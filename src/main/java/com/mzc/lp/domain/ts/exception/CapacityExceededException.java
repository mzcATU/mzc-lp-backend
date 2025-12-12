package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CapacityExceededException extends BusinessException {

    public CapacityExceededException() {
        super(ErrorCode.CAPACITY_EXCEEDED);
    }

    public CapacityExceededException(Long courseTimeId) {
        super(ErrorCode.CAPACITY_EXCEEDED,
                String.format("Capacity exceeded for CourseTime id: %d", courseTimeId));
    }

    public CapacityExceededException(int capacity, int currentEnrollment) {
        super(ErrorCode.CAPACITY_EXCEEDED,
                String.format("Capacity exceeded: %d/%d", currentEnrollment, capacity));
    }
}
