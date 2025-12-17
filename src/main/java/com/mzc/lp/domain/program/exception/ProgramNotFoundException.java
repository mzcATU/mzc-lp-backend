package com.mzc.lp.domain.program.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ProgramNotFoundException extends BusinessException {

    public ProgramNotFoundException() {
        super(ErrorCode.PROGRAM_NOT_FOUND);
    }

    public ProgramNotFoundException(Long programId) {
        super(ErrorCode.PROGRAM_NOT_FOUND, "프로그램을 찾을 수 없습니다. ID: " + programId);
    }
}
