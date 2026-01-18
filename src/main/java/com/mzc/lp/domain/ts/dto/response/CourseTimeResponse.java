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

public record CourseTimeResponse(
        Long id,
        Long courseId,
        String title,
        String courseTitle,
        DeliveryType deliveryType,
        DurationType durationType,
        CourseTimeStatus status,
        LocalDate enrollStartDate,
        LocalDate enrollEndDate,
        LocalDate classStartDate,
        LocalDate classEndDate,
        Integer durationDays,
        Integer capacity,
        Integer currentEnrollment,
        Integer availableSeats,
        EnrollmentMethod enrollmentMethod,
        BigDecimal price,
        boolean isFree,
        boolean allowLateEnrollment,
        RecurringScheduleResponse recurringSchedule,
        Instant createdAt,
        List<InstructorAssignmentResponse> instructors
) {
    public static CourseTimeResponse from(CourseTime entity) {
        return from(entity, List.of());
    }

    public static CourseTimeResponse from(CourseTime entity, List<InstructorAssignmentResponse> instructors) {
        return new CourseTimeResponse(
                entity.getId(),
                entity.getCourse() != null ? entity.getCourse().getId() : null,
                entity.getTitle(),
                entity.getCourse() != null ? entity.getCourse().getTitle() : null,
                entity.getDeliveryType(),
                entity.getDurationType(),
                entity.getStatus(),
                entity.getEnrollStartDate(),
                entity.getEnrollEndDate(),
                entity.getClassStartDate(),
                entity.getClassEndDate(),
                entity.getDurationDays(),
                entity.getCapacity(),
                entity.getCurrentEnrollment(),
                entity.getAvailableSeats(),
                entity.getEnrollmentMethod(),
                entity.getPrice(),
                entity.isFree(),
                entity.isAllowLateEnrollment(),
                RecurringScheduleResponse.from(entity.getRecurringSchedule()),
                entity.getCreatedAt(),
                instructors != null ? instructors : List.of()
        );
    }
}
