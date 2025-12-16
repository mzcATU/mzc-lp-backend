package com.mzc.lp.domain.snapshot.dto.response;

import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;

import java.time.Instant;

public record SnapshotResponse(
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
        Instant createdAt,
        Instant updatedAt
) {
    public static SnapshotResponse from(CourseSnapshot snapshot) {
        return new SnapshotResponse(
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
                snapshot.getCreatedAt(),
                snapshot.getUpdatedAt()
        );
    }
}
