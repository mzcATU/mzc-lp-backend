package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InvalidProgramException extends BusinessException {

    public InvalidProgramException(Long programId) {
        super(ErrorCode.INVALID_PROGRAM_FOR_ROADMAP,
            "로드맵에 추가할 수 없는 프로그램입니다. ID: " + programId);
    }

    public InvalidProgramException(String message) {
        super(ErrorCode.INVALID_PROGRAM_FOR_ROADMAP, message);
    }
}
