package com.mzc.lp.domain.category.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class CategoryNotFoundException extends BusinessException {

    public CategoryNotFoundException(Long categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND, "카테고리를 찾을 수 없습니다: " + categoryId);
    }
}
