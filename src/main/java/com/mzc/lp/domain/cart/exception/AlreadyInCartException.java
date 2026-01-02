package com.mzc.lp.domain.cart.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AlreadyInCartException extends BusinessException {

    public AlreadyInCartException() {
        super(ErrorCode.ALREADY_IN_CART);
    }

    public AlreadyInCartException(Long courseId) {
        super(ErrorCode.ALREADY_IN_CART, "Course already in cart: " + courseId);
    }
}
