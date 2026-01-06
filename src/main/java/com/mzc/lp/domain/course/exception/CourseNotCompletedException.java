package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseNotCompletedException extends BusinessException {

    public CourseNotCompletedException() {
        super(ErrorCode.CM_NOT_COMPLETED_COURSE);
    }

    public CourseNotCompletedException(Long courseId) {
        super(ErrorCode.CM_NOT_COMPLETED_COURSE,
            "수강 완료한 강의만 리뷰를 작성할 수 있습니다. CourseId: " + courseId);
    }
}
