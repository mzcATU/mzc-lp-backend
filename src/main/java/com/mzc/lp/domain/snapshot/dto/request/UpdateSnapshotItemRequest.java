package com.mzc.lp.domain.snapshot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSnapshotItemRequest(
        @NotBlank(message = "항목 이름은 필수입니다")
        @Size(max = 255, message = "항목 이름은 255자 이하여야 합니다")
        String itemName
) {
    public UpdateSnapshotItemRequest {
        if (itemName != null) {
            itemName = itemName.trim();
        }
    }
}
