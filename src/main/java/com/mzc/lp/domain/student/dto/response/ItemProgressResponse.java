package com.mzc.lp.domain.student.dto.response;

import com.mzc.lp.domain.student.entity.ItemProgress;

import java.time.Instant;

public record ItemProgressResponse(
        Long id,
        Long enrollmentId,
        Long itemId,
        Integer progressPercent,
        Integer watchedSeconds,
        Boolean completed,
        Instant completedAt,
        Integer lastPositionSeconds
) {
    public static ItemProgressResponse from(ItemProgress entity) {
        return new ItemProgressResponse(
                entity.getId(),
                entity.getEnrollmentId(),
                entity.getItemId(),
                entity.getProgressPercent(),
                entity.getWatchedSeconds(),
                entity.getCompleted(),
                entity.getCompletedAt(),
                entity.getLastPositionSeconds()
        );
    }
}