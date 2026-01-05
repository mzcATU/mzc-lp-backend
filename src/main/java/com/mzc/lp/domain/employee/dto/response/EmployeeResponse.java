package com.mzc.lp.domain.employee.dto.response;

import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.entity.Employee;

import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeNumber,
        Long userId,
        String userName,
        String userEmail,
        String userPhone,
        String profileImageUrl,
        Long departmentId,
        String departmentName,
        String departmentCode,
        String position,
        String jobTitle,
        LocalDate hireDate,
        LocalDate resignationDate,
        EmployeeStatus status,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getUser().getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(),
                employee.getUser().getProfileImageUrl(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getDepartment() != null ? employee.getDepartment().getCode() : null,
                employee.getPosition(),
                employee.getJobTitle(),
                employee.getHireDate(),
                employee.getResignationDate(),
                employee.getStatus(),
                employee.getSortOrder(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
