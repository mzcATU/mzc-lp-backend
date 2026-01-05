package com.mzc.lp.domain.cart.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException() {
        super(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    public CartItemNotFoundException(Long courseTimeId) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found for courseTimeId: " + courseTimeId);
    }
}
