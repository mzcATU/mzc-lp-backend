package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DuplicateProgramInRoadmapException extends BusinessException {

    public DuplicateProgramInRoadmapException(Long programId) {
        super(ErrorCode.DUPLICATE_PROGRAM_IN_ROADMAP,
            "이미 로드맵에 포함된 프로그램입니다. ID: " + programId);
    }
}
