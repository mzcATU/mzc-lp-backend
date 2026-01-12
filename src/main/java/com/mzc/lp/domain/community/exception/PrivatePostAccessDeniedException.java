package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class PrivatePostAccessDeniedException extends BusinessException {

    public PrivatePostAccessDeniedException() {
        super(ErrorCode.COMMUNITY_PRIVATE_POST_ACCESS_DENIED);
    }

    public PrivatePostAccessDeniedException(Long postId) {
        super(ErrorCode.COMMUNITY_PRIVATE_POST_ACCESS_DENIED, "Not authorized to access private post: " + postId);
    }
}
