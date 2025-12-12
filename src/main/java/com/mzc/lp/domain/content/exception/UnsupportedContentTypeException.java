package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class UnsupportedContentTypeException extends BusinessException {

    public UnsupportedContentTypeException() {
        super(ErrorCode.UNSUPPORTED_FILE_TYPE);
    }

    public UnsupportedContentTypeException(String extension) {
        super(ErrorCode.UNSUPPORTED_FILE_TYPE, "Unsupported file extension: " + extension);
    }
}
