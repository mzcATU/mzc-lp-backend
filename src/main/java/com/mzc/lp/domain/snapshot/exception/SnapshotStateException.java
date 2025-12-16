package com.mzc.lp.domain.snapshot.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;

public class SnapshotStateException extends BusinessException {

    public SnapshotStateException() {
        super(ErrorCode.CM_SNAPSHOT_STATE_ERROR);
    }

    public SnapshotStateException(String message) {
        super(ErrorCode.CM_SNAPSHOT_STATE_ERROR, message);
    }

    public SnapshotStateException(SnapshotStatus currentStatus, String action) {
        super(ErrorCode.CM_SNAPSHOT_STATE_ERROR,
              String.format("%s 상태에서는 %s 할 수 없습니다", currentStatus, action));
    }
}
