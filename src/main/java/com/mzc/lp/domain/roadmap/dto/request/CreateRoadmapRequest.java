package com.mzc.lp.domain.roadmap.dto.request;

import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoadmapRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        @NotEmpty(message = "최소 1개 이상의 프로그램이 필요합니다")
        List<Long> programIds,

        RoadmapStatus status
) {
}
