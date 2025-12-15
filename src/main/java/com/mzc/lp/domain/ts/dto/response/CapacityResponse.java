package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.entity.CourseTime;

public record CapacityResponse(
        Long courseTimeId,
        Integer capacity,
        Integer currentEnrollment,
        Integer availableSeats,
        boolean unlimited
) {
    public static CapacityResponse from(CourseTime courseTime) {
        return new CapacityResponse(
                courseTime.getId(),
                courseTime.getCapacity(),
                courseTime.getCurrentEnrollment(),
                courseTime.hasUnlimitedCapacity() ? null : courseTime.getAvailableSeats(),
                courseTime.hasUnlimitedCapacity()
        );
    }
}
