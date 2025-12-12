package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.ts.constant.DeliveryType;

public class LocationRequiredException extends BusinessException {

    public LocationRequiredException() {
        super(ErrorCode.LOCATION_REQUIRED);
    }

    public LocationRequiredException(DeliveryType deliveryType) {
        super(ErrorCode.LOCATION_REQUIRED,
                String.format("Location info is required for delivery type: %s", deliveryType));
    }
}
