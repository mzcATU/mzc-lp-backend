package com.mzc.lp.domain.snapshot.dto.response;

import com.mzc.lp.domain.snapshot.entity.SnapshotLearningObject;

public record SnapshotLearningObjectResponse(
        Long snapshotLoId,
        Long sourceLoId,
        Long contentId,
        String displayName,
        Integer duration,
        String thumbnailUrl,
        String resolution,
        String externalUrl,
        Boolean isCustomized
) {
    public static SnapshotLearningObjectResponse from(SnapshotLearningObject slo) {
        if (slo == null) {
            return null;
        }
        return new SnapshotLearningObjectResponse(
                slo.getId(),
                slo.getSourceLoId(),
                slo.getContentId(),
                slo.getDisplayName(),
                slo.getDuration(),
                slo.getThumbnailUrl(),
                slo.getResolution(),
                slo.getExternalUrl(),
                slo.getIsCustomized()
        );
    }
}
