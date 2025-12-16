package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CannotDeleteMainInstructorException extends BusinessException {

    public CannotDeleteMainInstructorException(Long assignmentId) {
        super(ErrorCode.CANNOT_DELETE_MAIN_INSTRUCTOR,
                String.format("Cannot delete main instructor (assignmentId=%d) while course is ongoing", assignmentId));
    }
}
