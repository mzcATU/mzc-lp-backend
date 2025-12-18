package com.mzc.lp.domain.snapshot.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class SnapshotItemNotFoundException extends BusinessException {

    public SnapshotItemNotFoundException() {
        super(ErrorCode.CM_SNAPSHOT_ITEM_NOT_FOUND);
    }

    public SnapshotItemNotFoundException(Long itemId) {
        super(ErrorCode.CM_SNAPSHOT_ITEM_NOT_FOUND, "스냅샷 항목을 찾을 수 없습니다. ID: " + itemId);
    }
}
