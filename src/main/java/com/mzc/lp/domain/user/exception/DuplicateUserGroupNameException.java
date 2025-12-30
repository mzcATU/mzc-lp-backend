package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DuplicateUserGroupNameException extends BusinessException {

    public DuplicateUserGroupNameException(String name) {
        super(ErrorCode.DUPLICATE_USER_GROUP_NAME, "User group name already exists: " + name);
    }
}
