package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class MainInstructorRequiredException extends BusinessException {

    public MainInstructorRequiredException() {
        super(ErrorCode.MAIN_INSTRUCTOR_REQUIRED);
    }

    public MainInstructorRequiredException(Long courseTimeId) {
        super(ErrorCode.MAIN_INSTRUCTOR_REQUIRED,
                String.format("Main instructor required for course time: %d", courseTimeId));
    }
}
