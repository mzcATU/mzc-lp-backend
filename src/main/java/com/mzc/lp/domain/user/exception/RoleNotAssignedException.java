package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class RoleNotAssignedException extends BusinessException {

    public RoleNotAssignedException() {
        super(ErrorCode.ROLE_NOT_ASSIGNED);
    }
}
