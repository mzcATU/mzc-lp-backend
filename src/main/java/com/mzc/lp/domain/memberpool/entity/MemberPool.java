package com.mzc.lp.domain.memberpool.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "member_pools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPool extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Embedded
    private MemberPoolCondition condition;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 정적 팩토리 메서드
    public static MemberPool create(String name, String description,
                                     List<Long> departmentIds, List<String> positions,
                                     List<String> jobTitles, List<String> employeeStatuses) {
        MemberPool pool = new MemberPool();
        pool.name = name;
        pool.description = description;
        pool.condition = MemberPoolCondition.create(departmentIds, positions, jobTitles, employeeStatuses);
        return pool;
    }

    // 비즈니스 메서드
    public void update(String name, String description,
                       List<Long> departmentIds, List<String> positions,
                       List<String> jobTitles, List<String> employeeStatuses) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        if (this.condition == null) {
            this.condition = MemberPoolCondition.create(departmentIds, positions, jobTitles, employeeStatuses);
        } else {
            this.condition.update(departmentIds, positions, jobTitles, employeeStatuses);
        }
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
