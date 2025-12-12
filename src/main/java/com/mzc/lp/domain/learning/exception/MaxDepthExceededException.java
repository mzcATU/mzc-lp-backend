package com.mzc.lp.domain.learning.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class MaxDepthExceededException extends BusinessException {

    public MaxDepthExceededException() {
        super(ErrorCode.MAX_FOLDER_DEPTH_EXCEEDED);
    }

    public MaxDepthExceededException(int maxDepth) {
        super(ErrorCode.MAX_FOLDER_DEPTH_EXCEEDED,
                "Maximum folder depth exceeded. Max depth is " + maxDepth + " (3 levels)");
    }
}
