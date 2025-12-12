package com.mzc.lp.domain.learning.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class ContentFolderNotFoundException extends BusinessException {

    public ContentFolderNotFoundException() {
        super(ErrorCode.CONTENT_FOLDER_NOT_FOUND);
    }

    public ContentFolderNotFoundException(Long folderId) {
        super(ErrorCode.CONTENT_FOLDER_NOT_FOUND, "Content folder not found with id: " + folderId);
    }
}
