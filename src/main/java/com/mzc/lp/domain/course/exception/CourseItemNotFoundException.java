package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseItemNotFoundException extends BusinessException {

    public CourseItemNotFoundException() {
        super(ErrorCode.CM_COURSE_ITEM_NOT_FOUND);
    }

    public CourseItemNotFoundException(Long itemId) {
        super(ErrorCode.CM_COURSE_ITEM_NOT_FOUND, "차시/폴더를 찾을 수 없습니다. ID: " + itemId);
    }
}
