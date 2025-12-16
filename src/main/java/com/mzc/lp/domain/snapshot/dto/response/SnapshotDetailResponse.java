package com.mzc.lp.domain.snapshot.dto.response;

import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;

import java.time.Instant;
import java.util.List;

public record SnapshotDetailResponse(
        Long snapshotId,
        String snapshotName,
        String description,
        String hashtags,
        Long sourceCourseId,
        String sourceCourseName,
        Long createdBy,
        SnapshotStatus status,
        Integer version,
        Long tenantId,
        List<SnapshotItemResponse> items,
        Long itemCount,
        Long totalDuration,
        Instant createdAt,
        Instant updatedAt
) {
    public static SnapshotDetailResponse from(CourseSnapshot snapshot, List<SnapshotItemResponse> items,
                                               Long itemCount, Long totalDuration) {
        return new SnapshotDetailResponse(
                snapshot.getId(),
                snapshot.getSnapshotName(),
                snapshot.getDescription(),
                snapshot.getHashtags(),
                snapshot.getSourceCourse() != null ? snapshot.getSourceCourse().getId() : null,
                snapshot.getSourceCourse() != null ? snapshot.getSourceCourse().getTitle() : null,
                snapshot.getCreatedBy(),
                snapshot.getStatus(),
                snapshot.getVersion(),
                snapshot.getTenantId(),
                items,
                itemCount,
                totalDuration,
                snapshot.getCreatedAt(),
                snapshot.getUpdatedAt()
        );
    }
}
