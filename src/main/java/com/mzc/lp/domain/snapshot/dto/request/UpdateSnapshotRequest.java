package com.mzc.lp.domain.snapshot.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSnapshotRequest(
        @Size(max = 255, message = "스냅샷 이름은 255자 이하여야 합니다")
        String snapshotName,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        @Size(max = 255, message = "해시태그는 255자 이하여야 합니다")
        String hashtags
) {
    public UpdateSnapshotRequest {
        if (snapshotName != null) {
            snapshotName = snapshotName.trim();
        }
    }
}
