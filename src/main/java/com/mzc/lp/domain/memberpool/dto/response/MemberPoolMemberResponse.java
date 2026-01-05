package com.mzc.lp.domain.memberpool.dto.response;

import com.mzc.lp.domain.employee.entity.Employee;

public record MemberPoolMemberResponse(
        Long userId,
        String name,
        String email,
        String employeeNumber,
        String departmentName,
        String position,
        String jobTitle,
        String status
) {
    public static MemberPoolMemberResponse from(Employee employee) {
        return new MemberPoolMemberResponse(
                employee.getUser().getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getEmployeeNumber(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getPosition(),
                employee.getJobTitle(),
                employee.getStatus().name()
        );
    }
}
