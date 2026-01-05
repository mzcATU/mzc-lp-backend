package com.mzc.lp.domain.memberpool.dto.request;

import java.util.List;

public record MemberPoolConditionDto(
        List<Long> departments,
        List<String> positions,
        List<String> jobTitles,
        List<String> employeeStatuses
) {
    public MemberPoolConditionDto {
        if (departments == null) departments = List.of();
        if (positions == null) positions = List.of();
        if (jobTitles == null) jobTitles = List.of();
        if (employeeStatuses == null) employeeStatuses = List.of();
    }
}
