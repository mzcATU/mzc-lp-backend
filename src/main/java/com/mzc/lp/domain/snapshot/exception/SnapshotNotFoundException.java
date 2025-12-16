package com.mzc.lp.domain.snapshot.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class SnapshotNotFoundException extends BusinessException {

    public SnapshotNotFoundException() {
        super(ErrorCode.CM_SNAPSHOT_NOT_FOUND);
    }

    public SnapshotNotFoundException(Long snapshotId) {
        super(ErrorCode.CM_SNAPSHOT_NOT_FOUND, "스냅샷을 찾을 수 없습니다. ID: " + snapshotId);
    }
}
