package com.mzc.lp.domain.iis.dto.response;

import java.time.LocalDate;

public record ScheduleConflictResponse(
        Long conflictingTimeId,
        String conflictingTimeTitle,
        LocalDate classStartDate,
        LocalDate classEndDate
) {
    public static ScheduleConflictResponse of(
            Long conflictingTimeId,
            String conflictingTimeTitle,
            LocalDate classStartDate,
            LocalDate classEndDate
    ) {
        return new ScheduleConflictResponse(
                conflictingTimeId,
                conflictingTimeTitle,
                classStartDate,
                classEndDate
        );
    }
}
