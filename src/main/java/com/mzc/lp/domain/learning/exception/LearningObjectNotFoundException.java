package com.mzc.lp.domain.learning.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class LearningObjectNotFoundException extends BusinessException {

    public LearningObjectNotFoundException() {
        super(ErrorCode.LEARNING_OBJECT_NOT_FOUND);
    }

    public LearningObjectNotFoundException(Long learningObjectId) {
        super(ErrorCode.LEARNING_OBJECT_NOT_FOUND, "Learning object not found with id: " + learningObjectId);
    }
}
