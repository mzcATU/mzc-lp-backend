package com.mzc.lp.domain.program.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.program.constant.ProgramStatus;

public class ProgramStatusException extends BusinessException {

    public ProgramStatusException(ProgramStatus currentStatus, String action) {
        super(ErrorCode.INVALID_PROGRAM_STATUS,
                String.format("현재 상태(%s)에서는 '%s' 작업을 수행할 수 없습니다", currentStatus, action));
    }
}
