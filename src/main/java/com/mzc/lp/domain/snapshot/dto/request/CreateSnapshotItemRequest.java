package com.mzc.lp.domain.snapshot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSnapshotItemRequest(
        @NotBlank(message = "항목 이름은 필수입니다")
        @Size(max = 255, message = "항목 이름은 255자 이하여야 합니다")
        String itemName,

        Long parentId,

        Long learningObjectId,

        @Size(max = 20, message = "항목 타입은 20자 이하여야 합니다")
        String itemType
) {
    public CreateSnapshotItemRequest {
        if (itemName != null) {
            itemName = itemName.trim();
        }
    }
}
