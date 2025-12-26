package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.time.LocalDate;

public record ConflictingAssignmentInfo(
        Long timeId,
        String courseName,
        String timeName,
        LocalDate startDate,
        LocalDate endDate,
        InstructorRole role
) {
    public static ConflictingAssignmentInfo of(
            CourseTime courseTime,
            InstructorRole role
    ) {
        return new ConflictingAssignmentInfo(
                courseTime.getId(),
                courseTime.getTitle(),
                courseTime.getTitle(),
                courseTime.getClassStartDate(),
                courseTime.getClassEndDate(),
                role
        );
    }
}
