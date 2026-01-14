package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 학습자용 CourseTime 목록 응답 DTO (카탈로그)
 */
public record CourseTimeCatalogResponse(
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
        CourseSummaryResponse course,
        List<InstructorSummaryResponse> instructors
) {
    private static final LocalDate ON_DEMAND_DATE = LocalDate.of(9999, 12, 31);

    public static CourseTimeCatalogResponse from(
            CourseTime courseTime,
            List<InstructorSummaryResponse> instructors
    ) {
        return from(courseTime, instructors, null);
    }

    public static CourseTimeCatalogResponse from(
            CourseTime courseTime,
            List<InstructorSummaryResponse> instructors,
            Category category
    ) {
        boolean isOnDemand = ON_DEMAND_DATE.equals(courseTime.getClassEndDate());
        int availableSeats = Math.max(0,
                courseTime.getCapacity() != null
                        ? courseTime.getCapacity() - courseTime.getCurrentEnrollment()
                        : Integer.MAX_VALUE);

        return new CourseTimeCatalogResponse(
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
                CourseSummaryResponse.forListWithCategory(courseTime.getCourse(), category),
                instructors != null ? instructors : List.of()
        );
    }
}
