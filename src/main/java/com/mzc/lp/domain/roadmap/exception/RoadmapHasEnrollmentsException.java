package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class RoadmapHasEnrollmentsException extends BusinessException {

    public RoadmapHasEnrollmentsException(Long roadmapId) {
        super(ErrorCode.ROADMAP_HAS_ENROLLMENTS,
            "수강생이 있는 로드맵은 삭제할 수 없습니다. ID: " + roadmapId);
    }
}
