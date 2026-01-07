package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotReviewOwnerException extends BusinessException {

    public NotReviewOwnerException() {
        super(ErrorCode.CM_NOT_REVIEW_OWNER);
    }

    public NotReviewOwnerException(Long reviewId, Long userId) {
        super(ErrorCode.CM_NOT_REVIEW_OWNER,
            "본인의 리뷰만 수정/삭제할 수 있습니다. ReviewId: " + reviewId + ", UserId: " + userId);
    }
}
