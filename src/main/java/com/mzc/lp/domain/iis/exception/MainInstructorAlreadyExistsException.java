package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class MainInstructorAlreadyExistsException extends BusinessException {

    public MainInstructorAlreadyExistsException() {
        super(ErrorCode.MAIN_INSTRUCTOR_ALREADY_EXISTS);
    }

    public MainInstructorAlreadyExistsException(Long timeId) {
        super(ErrorCode.MAIN_INSTRUCTOR_ALREADY_EXISTS,
                "Main instructor already exists for timeId: " + timeId);
    }
}
