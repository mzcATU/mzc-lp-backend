package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class PostNotFoundException extends BusinessException {

    public PostNotFoundException() {
        super(ErrorCode.COMMUNITY_POST_NOT_FOUND);
    }

    public PostNotFoundException(Long postId) {
        super(ErrorCode.COMMUNITY_POST_NOT_FOUND, "Post not found: " + postId);
    }
}
