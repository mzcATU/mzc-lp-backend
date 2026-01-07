package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

/**
 * 코스 커뮤니티 접근 시 수강 등록되지 않은 경우 발생하는 예외
 */
public class NotEnrolledException extends BusinessException {

    public NotEnrolledException() {
        super(ErrorCode.COMMUNITY_NOT_ENROLLED);
    }

    public NotEnrolledException(Long courseTimeId) {
        super(ErrorCode.COMMUNITY_NOT_ENROLLED, "Not enrolled in course time: " + courseTimeId);
    }
}
