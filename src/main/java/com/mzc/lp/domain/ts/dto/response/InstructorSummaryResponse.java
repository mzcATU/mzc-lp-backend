package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.user.entity.User;

public record InstructorSummaryResponse(
        Long id,
        String name,
        InstructorRole role,
        String profileImageUrl
) {
    public static InstructorSummaryResponse from(InstructorAssignment assignment, User user) {
        if (assignment == null || user == null) {
            return null;
        }
        return new InstructorSummaryResponse(
                user.getId(),
                user.getName(),
                assignment.getRole(),
                user.getProfileImageUrl()
        );
    }
}
