package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotPostAuthorException extends BusinessException {

    public NotPostAuthorException() {
        super(ErrorCode.COMMUNITY_NOT_POST_AUTHOR);
    }

    public NotPostAuthorException(Long postId) {
        super(ErrorCode.COMMUNITY_NOT_POST_AUTHOR, "Not authorized to modify post: " + postId);
    }
}
