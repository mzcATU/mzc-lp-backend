package com.mzc.lp.domain.program.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ProgramNotApprovedException extends BusinessException {

    public ProgramNotApprovedException() {
        super(ErrorCode.PROGRAM_NOT_APPROVED);
    }

    public ProgramNotApprovedException(Long programId) {
        super(ErrorCode.PROGRAM_NOT_APPROVED, "승인된 프로그램만 차수를 생성할 수 있습니다. ID: " + programId);
    }
}
