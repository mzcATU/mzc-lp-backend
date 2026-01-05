package com.mzc.lp.domain.department.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class DepartmentNotFoundException extends BusinessException {

    public DepartmentNotFoundException() {
        super(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    public DepartmentNotFoundException(Long departmentId) {
        super(ErrorCode.DEPARTMENT_NOT_FOUND, "부서를 찾을 수 없습니다. ID: " + departmentId);
    }
}
