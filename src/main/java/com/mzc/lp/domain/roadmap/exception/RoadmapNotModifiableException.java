package com.mzc.lp.domain.roadmap.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;

public class RoadmapNotModifiableException extends BusinessException {

    public RoadmapNotModifiableException(RoadmapStatus status) {
        super(ErrorCode.ROADMAP_NOT_MODIFIABLE,
            "현재 상태에서는 로드맵을 수정할 수 없습니다. 상태: " + status);
    }
}
