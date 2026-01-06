package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseReviewNotFoundException extends BusinessException {

    public CourseReviewNotFoundException() {
        super(ErrorCode.CM_REVIEW_NOT_FOUND);
    }

    public CourseReviewNotFoundException(Long reviewId) {
        super(ErrorCode.CM_REVIEW_NOT_FOUND, "리뷰를 찾을 수 없습니다. ID: " + reviewId);
    }
}
