package com.mzc.lp.domain.memberpool.dto.response;

import com.mzc.lp.domain.memberpool.entity.MemberPoolCondition;

import java.util.List;

public record MemberPoolConditionResponse(
        List<Long> departments,
        List<String> positions,
        List<String> jobTitles,
        List<String> employeeStatuses
) {
    public static MemberPoolConditionResponse from(MemberPoolCondition condition) {
        if (condition == null) {
            return new MemberPoolConditionResponse(List.of(), List.of(), List.of(), List.of());
        }
        return new MemberPoolConditionResponse(
                condition.getDepartmentIdList(),
                condition.getPositionList(),
                condition.getJobTitleList(),
                condition.getEmployeeStatusList()
        );
    }
}
