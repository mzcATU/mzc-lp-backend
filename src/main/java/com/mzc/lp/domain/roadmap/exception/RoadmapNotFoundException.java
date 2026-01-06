package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class RoadmapNotFoundException extends BusinessException {

    public RoadmapNotFoundException() {
        super(ErrorCode.ROADMAP_NOT_FOUND);
    }

    public RoadmapNotFoundException(Long roadmapId) {
        super(ErrorCode.ROADMAP_NOT_FOUND, "로드맵을 찾을 수 없습니다. ID: " + roadmapId);
    }
}
