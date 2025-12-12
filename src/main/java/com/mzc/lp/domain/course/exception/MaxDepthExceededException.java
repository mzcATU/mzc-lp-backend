package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class MaxDepthExceededException extends BusinessException {

    public MaxDepthExceededException() {
        super(ErrorCode.CM_MAX_DEPTH_EXCEEDED);
    }

    public MaxDepthExceededException(int currentDepth) {
        super(ErrorCode.CM_MAX_DEPTH_EXCEEDED, "최대 깊이(10단계)를 초과했습니다. 현재 깊이: " + currentDepth);
    }
}
