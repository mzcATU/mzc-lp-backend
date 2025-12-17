package com.mzc.lp.domain.program.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.program.constant.ProgramStatus;

public class ProgramNotModifiableException extends BusinessException {

    public ProgramNotModifiableException(ProgramStatus currentStatus) {
        super(ErrorCode.PROGRAM_NOT_MODIFIABLE,
                String.format("현재 상태(%s)에서는 수정할 수 없습니다", currentStatus));
    }
}
