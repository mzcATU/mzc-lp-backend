package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.InstructorRole;

import java.math.BigDecimal;

/**
 * 차수별 강사 통계 Response
 */
public record CourseTimeStatResponse(
        Long timeKey,
        String courseName,
        String timeName,
        InstructorRole role,
        Long totalStudents,
        Long completedStudents,
        BigDecimal completionRate
) {
    public static CourseTimeStatResponse of(
            Long timeKey,
            String courseName,
            String timeName,
            InstructorRole role,
            Long totalStudents,
            Long completedStudents,
            BigDecimal completionRate
    ) {
        return new CourseTimeStatResponse(
                timeKey,
                courseName,
                timeName,
                role,
                totalStudents,
                completedStudents,
                completionRate
        );
    }

    public static CourseTimeStatResponse withoutStudentStats(
            Long timeKey,
            String courseName,
            String timeName,
            InstructorRole role
    ) {
        return new CourseTimeStatResponse(
                timeKey,
                courseName,
                timeName,
                role,
                null,
                null,
                null
        );
    }
}
