package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AlreadyLikedException extends BusinessException {

    public AlreadyLikedException() {
        super(ErrorCode.COMMUNITY_ALREADY_LIKED);
    }

    public AlreadyLikedException(String message) {
        super(ErrorCode.COMMUNITY_ALREADY_LIKED, message);
    }
}
