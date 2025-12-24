package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;

import java.util.List;
import java.util.Map;

public record InstructorStatisticsResponse(
        Long totalAssignments,
        Long activeAssignments,
        Map<InstructorRole, Long> byRole,
        Map<AssignmentStatus, Long> byStatus,
        List<InstructorStatResponse> instructorStats
) {
    public static InstructorStatisticsResponse of(
            Long totalAssignments,
            Long activeAssignments,
            Map<InstructorRole, Long> byRole,
            Map<AssignmentStatus, Long> byStatus,
            List<InstructorStatResponse> instructorStats
    ) {
        return new InstructorStatisticsResponse(totalAssignments, activeAssignments, byRole, byStatus, instructorStats);
    }
}
