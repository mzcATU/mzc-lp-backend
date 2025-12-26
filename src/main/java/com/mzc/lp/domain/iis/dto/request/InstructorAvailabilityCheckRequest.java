package com.mzc.lp.domain.iis.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record InstructorAvailabilityCheckRequest(
        @NotEmpty(message = "userIds must not be empty")
        List<Long> userIds,

        @NotNull(message = "startDate is required")
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        LocalDate endDate
) {
}
