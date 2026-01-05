package com.mzc.lp.domain.employee.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateEmployeeRequest(
        @Size(max = 50, message = "사번은 50자 이하여야 합니다")
        @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "사번은 영문, 숫자, 밑줄, 하이픈만 사용 가능합니다")
        String employeeNumber,

        Long departmentId,

        @Size(max = 100, message = "직위는 100자 이하여야 합니다")
        String position,

        @Size(max = 100, message = "직책은 100자 이하여야 합니다")
        String jobTitle,

        LocalDate hireDate,

        Integer sortOrder
) {
    public UpdateEmployeeRequest {
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            employeeNumber = employeeNumber.toUpperCase().trim();
        }
    }
}
