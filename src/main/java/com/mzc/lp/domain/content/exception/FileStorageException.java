package com.mzc.lp.domain.content.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class FileStorageException extends BusinessException {

    public FileStorageException() {
        super(ErrorCode.FILE_UPLOAD_FAILED);
    }

    public FileStorageException(String message) {
        super(ErrorCode.FILE_UPLOAD_FAILED, message);
    }

    public FileStorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileStorageException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
