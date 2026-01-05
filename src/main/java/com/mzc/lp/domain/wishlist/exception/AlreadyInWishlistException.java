package com.mzc.lp.domain.wishlist.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AlreadyInWishlistException extends BusinessException {

    public AlreadyInWishlistException() {
        super(ErrorCode.ALREADY_IN_WISHLIST);
    }

    public AlreadyInWishlistException(Long userId, Long courseTimeId) {
        super(ErrorCode.ALREADY_IN_WISHLIST,
              "CourseTime " + courseTimeId + " is already in wishlist for user " + userId);
    }
}
