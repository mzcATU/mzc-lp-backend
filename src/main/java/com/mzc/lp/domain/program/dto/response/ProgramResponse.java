package com.mzc.lp.domain.program.dto.response;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;

import java.time.Instant;
import java.time.LocalDate;

public record ProgramResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        ProgramLevel level,
        ProgramType type,
        Integer estimatedHours,
        ProgramStatus status,
        Long createdBy,
        String creatorName,
        Long snapshotId,
        // OWNER 정보 (승인된 프로그램에만 존재)
        Long ownerId,
        String ownerName,
        String ownerEmail,
        // Course 권장 운영 기간 (차수 생성 시 기본값으로 활용)
        LocalDate courseStartDate,
        LocalDate courseEndDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProgramResponse from(Program program) {
        LocalDate courseStartDate = null;
        LocalDate courseEndDate = null;

        CourseSnapshot snapshot = program.getSnapshot();
        if (snapshot != null && snapshot.getSourceCourse() != null) {
            Course course = snapshot.getSourceCourse();
            courseStartDate = course.getStartDate();
            courseEndDate = course.getEndDate();
        }

        return new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                program.getStatus(),
                program.getCreatedBy(),
                null,
                snapshot != null ? snapshot.getId() : null,
                null,
                null,
                null,
                courseStartDate,
                courseEndDate,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }

    public static ProgramResponse from(Program program, String creatorName, Long ownerId, String ownerName, String ownerEmail) {
        LocalDate courseStartDate = null;
        LocalDate courseEndDate = null;

        CourseSnapshot snapshot = program.getSnapshot();
        if (snapshot != null && snapshot.getSourceCourse() != null) {
            Course course = snapshot.getSourceCourse();
            courseStartDate = course.getStartDate();
            courseEndDate = course.getEndDate();
        }

        return new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                program.getStatus(),
                program.getCreatedBy(),
                creatorName,
                snapshot != null ? snapshot.getId() : null,
                ownerId,
                ownerName,
                ownerEmail,
                courseStartDate,
                courseEndDate,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }
}
