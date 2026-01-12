package com.mzc.lp.domain.user.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.user.constant.CourseRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_course_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "user_id", "course_id", "role"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCourseRole extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "course_id")
    private Long courseId;  // null이면 테넌트 레벨 역할 (DESIGNER)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseRole role;

    private Integer revenueSharePercent;  // 수익 분배 비율 (B2C: 70%)

    /**
     * 테넌트 레벨 DESIGNER 역할 생성 (TenantRole.DESIGNER가 강의 개설 시)
     * courseId = null: 특정 강의가 아닌 테넌트 레벨 설계 권한
     */
    public static UserCourseRole createDesigner(User user) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = null;
        ucr.role = CourseRole.DESIGNER;
        return ucr;
    }

    /**
     * 강의별 DESIGNER 역할 생성 (강의 생성/승인 시 해당 강의의 설계자로 등록)
     */
    public static UserCourseRole createCourseDesigner(User user, Long courseId) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = courseId;
        ucr.role = CourseRole.DESIGNER;
        ucr.revenueSharePercent = 70;  // B2C 기본 70% (플랫폼 30%)
        return ucr;
    }

    /**
     * 강의별 INSTRUCTOR 역할 생성 (OPERATOR가 차수에 강사 배정 시)
     */
    public static UserCourseRole createInstructor(User user, Long courseId) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = courseId;
        ucr.role = CourseRole.INSTRUCTOR;
        return ucr;
    }

    /**
     * 범용 역할 생성 (OPERATOR 권한)
     */
    public static UserCourseRole create(User user, Long courseId, CourseRole role, Integer revenueSharePercent) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = courseId;
        ucr.role = role;
        ucr.revenueSharePercent = revenueSharePercent;
        return ucr;
    }
}
