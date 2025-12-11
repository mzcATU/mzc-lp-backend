package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class RoleAlreadyExistsException extends BusinessException {

    public RoleAlreadyExistsException() {
        super(ErrorCode.ROLE_ALREADY_EXISTS);
    }

    public RoleAlreadyExistsException(String role) {
        super(ErrorCode.ROLE_ALREADY_EXISTS, "Role already exists: " + role);
    }
}
