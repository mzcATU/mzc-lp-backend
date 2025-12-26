package com.mzc.lp.domain.iis.dto.response;

import java.util.List;

public record InstructorAvailabilityResponse(
        Long userId,
        boolean available,
        List<ConflictingAssignmentInfo> conflictingAssignments
) {
    public static InstructorAvailabilityResponse available(Long userId) {
        return new InstructorAvailabilityResponse(userId, true, List.of());
    }

    public static InstructorAvailabilityResponse unavailable(Long userId, List<ConflictingAssignmentInfo> conflicts) {
        return new InstructorAvailabilityResponse(userId, false, conflicts);
    }
}
