package com.mzc.lp.domain.employee.dto.response;

import com.mzc.lp.domain.employee.entity.Employee;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record CreateLmsAccountResponse(
        Long userId,
        String email,
        String name,
        Long employeeId,
        String employeeNumber,
        String department,
        String position,
        String jobTitle,
        String temporaryPassword,
        Instant createdAt
) {
    public static CreateLmsAccountResponse from(User user, Employee employee, String temporaryPassword) {
        String departmentName = employee.getDepartment() != null
                ? employee.getDepartment().getName()
                : null;

        return new CreateLmsAccountResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                employee.getId(),
                employee.getEmployeeNumber(),
                departmentName,
                employee.getPosition(),
                employee.getJobTitle(),
                temporaryPassword,
                user.getCreatedAt()
        );
    }
}
