package com.mzc.lp.domain.banner.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class BannerNotFoundException extends BusinessException {

    public BannerNotFoundException() {
        super(ErrorCode.BANNER_NOT_FOUND);
    }

    public BannerNotFoundException(Long bannerId) {
        super(ErrorCode.BANNER_NOT_FOUND, "배너를 찾을 수 없습니다. ID: " + bannerId);
    }
}
