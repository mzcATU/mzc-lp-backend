package com.mzc.lp.domain.wishlist.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class WishlistItemNotFoundException extends BusinessException {

    public WishlistItemNotFoundException() {
        super(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
    }

    public WishlistItemNotFoundException(Long userId, Long courseTimeId) {
        super(ErrorCode.WISHLIST_ITEM_NOT_FOUND,
              "Wishlist item not found for user " + userId + " and courseTime " + courseTimeId);
    }
}
