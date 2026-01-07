package com.mzc.lp.domain.roadmap.dto.response;

import com.mzc.lp.domain.roadmap.entity.Roadmap;

import java.time.Instant;
import java.util.List;

public record RoadmapDetailResponse(
        Long id,
        String title,
        String description,
        Integer courseCount,
        Integer enrolledStudents,
        String status,
        Instant createdAt,
        Instant updatedAt,
        List<RoadmapProgramDto> programs
) {
    public static RoadmapDetailResponse from(Roadmap roadmap, List<RoadmapProgramDto> programs) {
        return new RoadmapDetailResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                programs.size(),
                roadmap.getEnrolledStudents(),
                roadmap.getStatus().name().toLowerCase(),
                roadmap.getCreatedAt(),
                roadmap.getUpdatedAt(),
                programs
        );
    }
}
