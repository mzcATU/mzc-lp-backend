package com.mzc.lp.domain.department.dto.response;

import com.mzc.lp.domain.user.entity.User;

public record DepartmentMemberResponse(
        Long id,
        String name,
        String email,
        String phone,
        String position,
        String role
) {
    public static DepartmentMemberResponse from(User user) {
        return new DepartmentMemberResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getPosition(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}
