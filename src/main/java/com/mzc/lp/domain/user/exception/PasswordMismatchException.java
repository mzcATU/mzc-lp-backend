package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class PasswordMismatchException extends BusinessException {

    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}
