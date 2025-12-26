package com.mzc.lp.domain.program.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ProgramOwnershipException extends BusinessException {

    public ProgramOwnershipException() {
        super(ErrorCode.UNAUTHORIZED_PROGRAM_ACCESS);
    }

    public ProgramOwnershipException(String message) {
        super(ErrorCode.UNAUTHORIZED_PROGRAM_ACCESS, message);
    }
}
