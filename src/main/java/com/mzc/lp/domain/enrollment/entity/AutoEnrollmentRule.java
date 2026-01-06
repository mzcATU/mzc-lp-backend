package com.mzc.lp.domain.enrollment.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auto_enrollment_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoEnrollmentRule extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private AutoEnrollmentTrigger trigger;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 정적 팩토리 메서드
    public static AutoEnrollmentRule create(String name, AutoEnrollmentTrigger trigger, Long courseTimeId) {
        AutoEnrollmentRule rule = new AutoEnrollmentRule();
        rule.name = name;
        rule.trigger = trigger;
        rule.courseTimeId = courseTimeId;
        return rule;
    }

    public static AutoEnrollmentRule createForDepartment(String name, Long departmentId, Long courseTimeId) {
        AutoEnrollmentRule rule = create(name, AutoEnrollmentTrigger.DEPARTMENT_ASSIGN, courseTimeId);
        rule.departmentId = departmentId;
        return rule;
    }

    // 비즈니스 메서드
    public void update(String name, String description, AutoEnrollmentTrigger trigger,
                       Long departmentId, Long courseTimeId) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        if (trigger != null) {
            this.trigger = trigger;
        }
        this.departmentId = departmentId;
        if (courseTimeId != null) {
            this.courseTimeId = courseTimeId;
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
