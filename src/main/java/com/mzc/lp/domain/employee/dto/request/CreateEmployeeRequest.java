package com.mzc.lp.domain.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank(message = "사번은 필수입니다")
        @Size(max = 50, message = "사번은 50자 이하여야 합니다")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "사번은 영문, 숫자, 밑줄, 하이픈만 사용 가능합니다")
        String employeeNumber,

        @NotNull(message = "사용자 ID는 필수입니다")
        Long userId,

        Long departmentId,

        @Size(max = 100, message = "직위는 100자 이하여야 합니다")
        String position,

        @Size(max = 100, message = "직책은 100자 이하여야 합니다")
        String jobTitle,

        LocalDate hireDate,

        Integer sortOrder
) {
    public CreateEmployeeRequest {
        if (employeeNumber != null) {
            employeeNumber = employeeNumber.toUpperCase().trim();
        }
    }
}
