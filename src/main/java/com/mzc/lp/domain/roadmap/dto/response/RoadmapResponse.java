package com.mzc.lp.domain.roadmap.dto.response;

import com.mzc.lp.domain.roadmap.entity.Roadmap;

import java.time.Instant;

public record RoadmapResponse(
        Long id,
        String title,
        String description,
        Integer courseCount,
        Integer enrolledStudents,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RoadmapResponse from(Roadmap roadmap, Integer courseCount) {
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                courseCount,
                roadmap.getEnrolledStudents(),
                roadmap.getStatus().name().toLowerCase(),
                roadmap.getCreatedAt(),
                roadmap.getUpdatedAt()
        );
    }
}
