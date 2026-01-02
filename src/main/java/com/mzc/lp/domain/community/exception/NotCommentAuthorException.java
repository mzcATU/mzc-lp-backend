package com.mzc.lp.domain.community.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class NotCommentAuthorException extends BusinessException {

    public NotCommentAuthorException() {
        super(ErrorCode.COMMUNITY_NOT_COMMENT_AUTHOR);
    }

    public NotCommentAuthorException(Long commentId) {
        super(ErrorCode.COMMUNITY_NOT_COMMENT_AUTHOR, "Not authorized to modify comment: " + commentId);
    }
}
