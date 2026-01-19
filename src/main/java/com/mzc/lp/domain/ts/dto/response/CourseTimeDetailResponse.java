package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CourseTimeDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String courseDescription,
        String courseCategory,
        String courseDifficulty,
        String courseThumbnailUrl,
        Long snapshotId,
        String title,
        String description,
        DeliveryType deliveryType,
        DurationType durationType,
        CourseTimeStatus status,
        LocalDate enrollStartDate,
        LocalDate enrollEndDate,
        LocalDate classStartDate,
        LocalDate classEndDate,
        Integer durationDays,
        Integer capacity,
        Integer maxWaitingCount,
        Integer currentEnrollment,
        Integer availableSeats,
        EnrollmentMethod enrollmentMethod,
        Integer minProgressForCompletion,
        BigDecimal price,
        boolean isFree,
        String locationInfo,
        boolean allowLateEnrollment,
        RecurringScheduleResponse recurringSchedule,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<InstructorAssignmentResponse> instructors
) {
    public static CourseTimeDetailResponse from(CourseTime entity) {
        return from(entity, null, List.of());
    }

    public static CourseTimeDetailResponse from(CourseTime entity, List<InstructorAssignmentResponse> instructors) {
        return from(entity, null, instructors);
    }

    public static CourseTimeDetailResponse from(CourseTime entity, String categoryName, List<InstructorAssignmentResponse> instructors) {
        var course = entity.getCourse();
        return new CourseTimeDetailResponse(
                entity.getId(),
                course != null ? course.getId() : null,
                course != null ? course.getTitle() : null,
                course != null ? course.getDescription() : null,
                categoryName,
                course != null && course.getLevel() != null ? course.getLevel().name() : null,
                course != null ? course.getThumbnailUrl() : null,
                entity.getSnapshot() != null ? entity.getSnapshot().getId() : null,
                entity.getTitle(),
                entity.getDescription(),
                entity.getDeliveryType(),
                entity.getDurationType(),
                entity.getStatus(),
                entity.getEnrollStartDate(),
                entity.getEnrollEndDate(),
                entity.getClassStartDate(),
                entity.getClassEndDate(),
                entity.getDurationDays(),
                entity.getCapacity(),
                entity.getMaxWaitingCount(),
                entity.getCurrentEnrollment(),
                entity.getAvailableSeats(),
                entity.getEnrollmentMethod(),
                entity.getMinProgressForCompletion(),
                entity.getPrice(),
                entity.isFree(),
                entity.getLocationInfo(),
                entity.isAllowLateEnrollment(),
                RecurringScheduleResponse.from(entity.getRecurringSchedule()),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                instructors != null ? instructors : List.of()
        );
    }
}
