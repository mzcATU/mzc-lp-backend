package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CommentNotFoundException extends BusinessException {

    public CommentNotFoundException() {
        super(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
    }

    public CommentNotFoundException(Long commentId) {
        super(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "Comment not found: " + commentId);
    }
}
