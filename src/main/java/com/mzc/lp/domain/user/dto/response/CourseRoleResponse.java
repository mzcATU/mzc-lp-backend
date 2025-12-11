package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.UserCourseRole;

import java.time.Instant;

public record CourseRoleResponse(
        Long courseRoleId,
        Long courseId,
        String courseName,
        String role,
        Integer revenueSharePercent,
        Instant createdAt
) {
    public static CourseRoleResponse from(UserCourseRole userCourseRole) {
        return new CourseRoleResponse(
                userCourseRole.getId(),
                userCourseRole.getCourseId(),
                null,  // courseName은 Course 모듈 구현 후 조인해서 가져올 예정
                userCourseRole.getRole().name(),
                userCourseRole.getRevenueSharePercent(),
                userCourseRole.getCreatedAt()
        );
    }

    public static CourseRoleResponse from(UserCourseRole userCourseRole, String courseName) {
        return new CourseRoleResponse(
                userCourseRole.getId(),
                userCourseRole.getCourseId(),
                courseName,
                userCourseRole.getRole().name(),
                userCourseRole.getRevenueSharePercent(),
                userCourseRole.getCreatedAt()
        );
    }
}
