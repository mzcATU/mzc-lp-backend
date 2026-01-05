package com.mzc.lp.domain.department.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class DepartmentCodeDuplicateException extends BusinessException {

    public DepartmentCodeDuplicateException(String code) {
        super(ErrorCode.DEPARTMENT_CODE_DUPLICATE, "이미 존재하는 부서 코드입니다: " + code);
    }
}
