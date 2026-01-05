package com.mzc.lp.domain.employee.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class EmployeeNotFoundException extends BusinessException {

    public EmployeeNotFoundException() {
        super(ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    public EmployeeNotFoundException(Long employeeId) {
        super(ErrorCode.EMPLOYEE_NOT_FOUND, "임직원을 찾을 수 없습니다. ID: " + employeeId);
    }
}
