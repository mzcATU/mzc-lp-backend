package com.mzc.lp.domain.memberpool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPoolCondition {

    @Column(name = "department_ids", length = 1000)
    private String departmentIds;

    @Column(name = "positions", length = 1000)
    private String positions;

    @Column(name = "job_titles", length = 1000)
    private String jobTitles;

    @Column(name = "employee_statuses", length = 500)
    private String employeeStatuses;

    public static MemberPoolCondition create(List<Long> departmentIds, List<String> positions,
                                              List<String> jobTitles, List<String> employeeStatuses) {
        MemberPoolCondition condition = new MemberPoolCondition();
        condition.departmentIds = joinLongs(departmentIds);
        condition.positions = joinStrings(positions);
        condition.jobTitles = joinStrings(jobTitles);
        condition.employeeStatuses = joinStrings(employeeStatuses);
        return condition;
    }

    public List<Long> getDepartmentIdList() {
        return parseLongs(departmentIds);
    }

    public List<String> getPositionList() {
        return parseStrings(positions);
    }

    public List<String> getJobTitleList() {
        return parseStrings(jobTitles);
    }

    public List<String> getEmployeeStatusList() {
        return parseStrings(employeeStatuses);
    }

    public void update(List<Long> departmentIds, List<String> positions,
                       List<String> jobTitles, List<String> employeeStatuses) {
        this.departmentIds = joinLongs(departmentIds);
        this.positions = joinStrings(positions);
        this.jobTitles = joinStrings(jobTitles);
        this.employeeStatuses = joinStrings(employeeStatuses);
    }

    private static String joinLongs(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values.stream().map(String::valueOf).toList());
    }

    private static String joinStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }

    private static List<Long> parseLongs(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (String s : value.split(",")) {
            result.add(Long.parseLong(s.trim()));
        }
        return result;
    }

    private static List<String> parseStrings(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String s : value.split(",")) {
            result.add(s.trim());
        }
        return result;
    }
}
