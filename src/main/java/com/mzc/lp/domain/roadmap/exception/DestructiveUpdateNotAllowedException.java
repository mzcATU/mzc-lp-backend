package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DestructiveUpdateNotAllowedException extends BusinessException {

    public DestructiveUpdateNotAllowedException(Long roadmapId) {
        super(ErrorCode.DESTRUCTIVE_UPDATE_NOT_ALLOWED,
            "수강생이 있는 공개된 로드맵은 프로그램 삭제 또는 순서 변경이 불가능합니다. ID: " + roadmapId);
    }
}
