package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateItemNameRequest(
        @NotBlank(message = "항목 이름은 필수입니다")
        @Size(max = 255, message = "항목 이름은 255자 이하여야 합니다")
        String itemName
) {
    public UpdateItemNameRequest {
        if (itemName != null) {
            itemName = itemName.trim();
        }
    }
}
