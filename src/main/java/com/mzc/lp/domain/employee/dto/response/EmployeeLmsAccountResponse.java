package com.mzc.lp.domain.employee.dto.response;

import com.mzc.lp.domain.employee.entity.Employee;

import java.time.Instant;

public record EmployeeLmsAccountResponse(
        boolean hasAccount,
        Long userId,
        String email,
        String name,
        Long employeeId,
        String employeeNumber,
        String employeeName,
        String department,
        String position,
        String jobTitle,
        Instant syncedAt
) {
    public static EmployeeLmsAccountResponse withAccount(Employee employee) {
        String departmentName = employee.getDepartment() != null
                ? employee.getDepartment().getName()
                : null;

        return new EmployeeLmsAccountResponse(
                true,
                employee.getUser().getId(),
                employee.getUser().getEmail(),
                employee.getUser().getName(),
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getUser().getName(),
                departmentName,
                employee.getPosition(),
                employee.getJobTitle(),
                employee.getUpdatedAt()
        );
    }

    public static EmployeeLmsAccountResponse withoutAccount(Long employeeId, String employeeNumber, String employeeName) {
        return new EmployeeLmsAccountResponse(
                false,
                null,
                null,
                null,
                employeeId,
                employeeNumber,
                employeeName,
                null,
                null,
                null,
                null
        );
    }
}
