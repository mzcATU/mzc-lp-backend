package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("removal")
public record CourseTimeResponse(
        Long id,
        @Deprecated(since = "1.0", forRemoval = true)
        Long cmCourseId,
        @Deprecated(since = "1.0", forRemoval = true)
        Long cmCourseVersionId,
        String title,
        DeliveryType deliveryType,
        CourseTimeStatus status,
        LocalDate enrollStartDate,
        LocalDate enrollEndDate,
        LocalDate classStartDate,
        LocalDate classEndDate,
        Integer capacity,
        Integer currentEnrollment,
        Integer availableSeats,
        EnrollmentMethod enrollmentMethod,
        BigDecimal price,
        boolean isFree,
        boolean allowLateEnrollment,
        Instant createdAt,
        List<InstructorAssignmentResponse> instructors
) {
    public static CourseTimeResponse from(CourseTime entity) {
        return from(entity, List.of());
    }

    public static CourseTimeResponse from(CourseTime entity, List<InstructorAssignmentResponse> instructors) {
        return new CourseTimeResponse(
                entity.getId(),
                entity.getCmCourseId(),
                entity.getCmCourseVersionId(),
                entity.getTitle(),
                entity.getDeliveryType(),
                entity.getStatus(),
                entity.getEnrollStartDate(),
                entity.getEnrollEndDate(),
                entity.getClassStartDate(),
                entity.getClassEndDate(),
                entity.getCapacity(),
                entity.getCurrentEnrollment(),
                entity.getAvailableSeats(),
                entity.getEnrollmentMethod(),
                entity.getPrice(),
                entity.isFree(),
                entity.isAllowLateEnrollment(),
                entity.getCreatedAt(),
                instructors != null ? instructors : List.of()
        );
    }
}
