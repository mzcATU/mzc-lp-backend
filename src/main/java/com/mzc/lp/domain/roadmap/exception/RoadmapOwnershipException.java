package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class RoadmapOwnershipException extends BusinessException {

    public RoadmapOwnershipException(Long roadmapId) {
        super(ErrorCode.UNAUTHORIZED_ROADMAP_ACCESS,
            "이 로드맵에 대한 권한이 없습니다. ID: " + roadmapId);
    }
}
