package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateDisplayInfoRequest(
        @Size(max = 255, message = "표시 이름은 255자 이하여야 합니다")
        String displayName,

        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
        String description
) {
    public UpdateDisplayInfoRequest {
        if (displayName != null) {
            displayName = displayName.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }
}
