package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 학습자용 CourseTime 상세 응답 DTO
 */
public record CourseTimePublicDetailResponse(
        Long id,
        String title,
        CourseTimeStatus status,
        DeliveryType deliveryType,
        DurationType durationType,
        LocalDate enrollStartDate,
        LocalDate enrollEndDate,
        LocalDate classStartDate,
        LocalDate classEndDate,
        Integer durationDays,
        Integer capacity,
        Integer currentEnrollment,
        Integer availableSeats,
        BigDecimal price,
        boolean isFree,
        EnrollmentMethod enrollmentMethod,
        boolean allowLateEnrollment,
        Integer minProgressForCompletion,
        String locationInfo,
        CourseSummaryResponse course,
        List<CurriculumItemResponse> curriculum,
        List<InstructorSummaryResponse> instructors
) {
    public static CourseTimePublicDetailResponse from(
            CourseTime courseTime,
            List<CurriculumItemResponse> curriculum,
            List<InstructorSummaryResponse> instructors
    ) {
        int availableSeats = Math.max(0,
                courseTime.getCapacity() != null
                        ? courseTime.getCapacity() - courseTime.getCurrentEnrollment()
                        : Integer.MAX_VALUE);

        return new CourseTimePublicDetailResponse(
                courseTime.getId(),
                courseTime.getTitle(),
                courseTime.getStatus(),
                courseTime.getDeliveryType(),
                courseTime.getDurationType(),
                courseTime.getEnrollStartDate(),
                courseTime.getEnrollEndDate(),
                courseTime.getClassStartDate(),
                courseTime.getClassEndDate(),
                courseTime.getDurationDays(),
                courseTime.getCapacity(),
                courseTime.getCurrentEnrollment(),
                availableSeats,
                courseTime.getPrice(),
                courseTime.isFree(),
                courseTime.getEnrollmentMethod(),
                courseTime.isAllowLateEnrollment(),
                courseTime.getMinProgressForCompletion(),
                courseTime.getLocationInfo(),
                CourseSummaryResponse.from(courseTime.getCourse()),
                curriculum != null ? curriculum : List.of(),
                instructors != null ? instructors : List.of()
        );
    }
}
