package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;

import java.time.LocalDate;

/**
 * 차수 생성 폼 초기값 응답 DTO
 * Course 정보 기반으로 기본값 제공
 */
public record CourseTimeFormDataResponse(
        Long courseId,
        String courseTitle,
        DeliveryType suggestedDeliveryType,
        DurationType suggestedDurationType,
        Integer suggestedDurationDays,
        LocalDate suggestedClassStartDate,
        LocalDate suggestedClassEndDate,
        CourseDefaults courseDefaults
) {
    public record CourseDefaults(
            CourseType type,
            Integer estimatedHours,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static CourseDefaults from(Course course) {
            return new CourseDefaults(
                    course.getType(),
                    course.getEstimatedHours(),
                    course.getStartDate(),
                    course.getEndDate()
            );
        }
    }

    public static CourseTimeFormDataResponse from(Course course) {
        DeliveryType suggestedDeliveryType = mapCourseTypeToDeliveryType(course.getType());
        DurationType suggestedDurationType = determineDurationType(course);
        Integer suggestedDurationDays = calculateSuggestedDurationDays(course);
        LocalDate suggestedClassStartDate = course.getStartDate();
        LocalDate suggestedClassEndDate = course.getEndDate();

        return new CourseTimeFormDataResponse(
                course.getId(),
                course.getTitle(),
                suggestedDeliveryType,
                suggestedDurationType,
                suggestedDurationDays,
                suggestedClassStartDate,
                suggestedClassEndDate,
                CourseDefaults.from(course)
        );
    }

    private static DeliveryType mapCourseTypeToDeliveryType(CourseType courseType) {
        if (courseType == null) {
            return DeliveryType.ONLINE;
        }
        return switch (courseType) {
            case ONLINE -> DeliveryType.ONLINE;
            case OFFLINE -> DeliveryType.OFFLINE;
            case BLENDED -> DeliveryType.BLENDED;
        };
    }

    private static DurationType determineDurationType(Course course) {
        if (course.getStartDate() != null && course.getEndDate() != null) {
            return DurationType.FIXED;
        }
        if (course.getEstimatedHours() != null) {
            return DurationType.RELATIVE;
        }
        return DurationType.UNLIMITED;
    }

    private static Integer calculateSuggestedDurationDays(Course course) {
        if (course.getStartDate() != null && course.getEndDate() != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(
                    course.getStartDate(), course.getEndDate()) + 1;
        }
        if (course.getEstimatedHours() != null) {
            return Math.max(1, (int) Math.ceil(course.getEstimatedHours() / 8.0));
        }
        return null;
    }
}
