package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
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
        boolean isOnDemand,
        LocalDate enrollStartDate,
        LocalDate enrollEndDate,
        LocalDate classStartDate,
        LocalDate classEndDate,
        Integer capacity,
        Integer currentEnrollment,
        Integer availableSeats,
        BigDecimal price,
        boolean isFree,
        EnrollmentMethod enrollmentMethod,
        boolean allowLateEnrollment,
        Integer minProgressForCompletion,
        String locationInfo,
        ProgramSummaryResponse program,
        List<CurriculumItemResponse> curriculum,
        List<InstructorSummaryResponse> instructors
) {
    private static final LocalDate ON_DEMAND_DATE = LocalDate.of(9999, 12, 31);

    public static CourseTimePublicDetailResponse from(
            CourseTime courseTime,
            List<CurriculumItemResponse> curriculum,
            List<InstructorSummaryResponse> instructors
    ) {
        boolean isOnDemand = ON_DEMAND_DATE.equals(courseTime.getClassEndDate());
        int availableSeats = Math.max(0,
                courseTime.getCapacity() != null
                        ? courseTime.getCapacity() - courseTime.getCurrentEnrollment()
                        : Integer.MAX_VALUE);

        return new CourseTimePublicDetailResponse(
                courseTime.getId(),
                courseTime.getTitle(),
                courseTime.getStatus(),
                courseTime.getDeliveryType(),
                isOnDemand,
                courseTime.getEnrollStartDate(),
                courseTime.getEnrollEndDate(),
                courseTime.getClassStartDate(),
                courseTime.getClassEndDate(),
                courseTime.getCapacity(),
                courseTime.getCurrentEnrollment(),
                availableSeats,
                courseTime.getPrice(),
                courseTime.isFree(),
                courseTime.getEnrollmentMethod(),
                courseTime.isAllowLateEnrollment(),
                courseTime.getMinProgressForCompletion(),
                courseTime.getLocationInfo(),
                ProgramSummaryResponse.from(courseTime.getProgram()),
                curriculum != null ? curriculum : List.of(),
                instructors != null ? instructors : List.of()
        );
    }
}
