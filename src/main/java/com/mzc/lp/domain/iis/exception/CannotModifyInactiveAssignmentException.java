package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CannotModifyInactiveAssignmentException extends BusinessException {

    public CannotModifyInactiveAssignmentException() {
        super(ErrorCode.CANNOT_MODIFY_INACTIVE_ASSIGNMENT);
    }

    public CannotModifyInactiveAssignmentException(Long assignmentId) {
        super(ErrorCode.CANNOT_MODIFY_INACTIVE_ASSIGNMENT,
                "Cannot modify inactive assignment: " + assignmentId);
    }
}
