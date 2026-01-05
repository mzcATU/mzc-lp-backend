package com.mzc.lp.domain.employee.dto.request;

import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChangeEmployeeStatusRequest(
        @NotNull(message = "상태는 필수입니다")
        EmployeeStatus status,

        LocalDate resignationDate
) {}
