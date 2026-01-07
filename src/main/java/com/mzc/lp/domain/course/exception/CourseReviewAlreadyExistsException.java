package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseReviewAlreadyExistsException extends BusinessException {

    public CourseReviewAlreadyExistsException() {
        super(ErrorCode.CM_REVIEW_ALREADY_EXISTS);
    }

    public CourseReviewAlreadyExistsException(Long courseId, Long userId) {
        super(ErrorCode.CM_REVIEW_ALREADY_EXISTS,
            "이미 작성한 리뷰가 있습니다. CourseId: " + courseId + ", UserId: " + userId);
    }
}
