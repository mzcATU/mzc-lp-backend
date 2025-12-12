package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank(message = "항목 이름은 필수입니다")
        @Size(max = 255, message = "항목 이름은 255자 이하여야 합니다")
        String itemName,

        Long parentId,

        @NotNull(message = "학습 객체 ID는 필수입니다")
        Long learningObjectId
) {
    public CreateItemRequest {
        if (itemName != null) {
            itemName = itemName.trim();
        }
    }
}
