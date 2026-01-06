package com.mzc.lp.domain.roadmap.dto.request;

import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRoadmapRequest(
        @Size(max = 255, message = "제목은 255자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        List<Long> programIds,

        RoadmapStatus status
) {
}
