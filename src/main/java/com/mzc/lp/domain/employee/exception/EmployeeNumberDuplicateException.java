package com.mzc.lp.domain.employee.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class EmployeeNumberDuplicateException extends BusinessException {

    public EmployeeNumberDuplicateException() {
        super(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
    }

    public EmployeeNumberDuplicateException(String employeeNumber) {
        super(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE, "이미 사용 중인 사번입니다: " + employeeNumber);
    }
}
