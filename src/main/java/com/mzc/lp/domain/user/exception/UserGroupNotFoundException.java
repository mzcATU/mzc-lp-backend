package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class UserGroupNotFoundException extends BusinessException {

    public UserGroupNotFoundException(String message) {
        super(ErrorCode.USER_GROUP_NOT_FOUND, message);
    }

    public UserGroupNotFoundException(Long groupId) {
        super(ErrorCode.USER_GROUP_NOT_FOUND, "User group not found: " + groupId);
    }
}
