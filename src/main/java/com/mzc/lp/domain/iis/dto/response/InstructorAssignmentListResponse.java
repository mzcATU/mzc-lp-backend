package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.time.LocalDate;

public record InstructorAssignmentListResponse(
        Long id,
        InstructorInfo instructor,
        CourseTimeInfo courseTime,
        ProgramInfo program,
        InstructorRole role,
        AssignmentStatus status,
        Instant assignedAt,
        Instant createdAt
) {
    public record InstructorInfo(
            Long id,
            String name,
            String email
    ) {
        public static InstructorInfo from(User user) {
            if (user == null) {
                return null;
            }
            return new InstructorInfo(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );
        }
    }

    public record CourseTimeInfo(
            Long id,
            String title,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static CourseTimeInfo from(CourseTime courseTime) {
            if (courseTime == null) {
                return null;
            }
            return new CourseTimeInfo(
                    courseTime.getId(),
                    courseTime.getTitle(),
                    courseTime.getClassStartDate(),
                    courseTime.getClassEndDate()
            );
        }
    }

    public record ProgramInfo(
            Long id,
            String title
    ) {
        public static ProgramInfo from(Program program) {
            if (program == null) {
                return null;
            }
            return new ProgramInfo(
                    program.getId(),
                    program.getTitle()
            );
        }
    }

    public static InstructorAssignmentListResponse from(
            InstructorAssignment entity,
            User user,
            CourseTime courseTime,
            Program program
    ) {
        return new InstructorAssignmentListResponse(
                entity.getId(),
                InstructorInfo.from(user),
                CourseTimeInfo.from(courseTime),
                ProgramInfo.from(program),
                entity.getRole(),
                entity.getStatus(),
                entity.getAssignedAt(),
                entity.getCreatedAt()
        );
    }
}
