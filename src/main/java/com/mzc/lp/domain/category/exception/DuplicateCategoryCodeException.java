package com.mzc.lp.domain.category.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class DuplicateCategoryCodeException extends BusinessException {

    public DuplicateCategoryCodeException(String code) {
        super(ErrorCode.DUPLICATE_CATEGORY_CODE, "이미 존재하는 카테고리 코드입니다: " + code);
    }
}
