package com.mzc.lp.domain.user.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import com.mzc.lp.domain.user.constant.CourseRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_course_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id", "role"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCourseRole extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "course_id")
    private Long courseId;  // null이면 테넌트 레벨 역할 (DESIGNER)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseRole role;

    private Integer revenueSharePercent;  // 수익 분배 비율 (B2C OWNER: 70%)

    // B2C: 강의 개설 버튼 클릭 시 DESIGNER 부여
    public static UserCourseRole createDesigner(User user) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = null;  // 아직 강의 없음
        ucr.role = CourseRole.DESIGNER;
        return ucr;
    }

    // 강의 승인 후 OWNER로 전환
    public static UserCourseRole createOwner(User user, Long courseId) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = courseId;
        ucr.role = CourseRole.OWNER;
        ucr.revenueSharePercent = 70;  // B2C 기본 70% (플랫폼 30%)
        return ucr;
    }

    // B2B: OPERATOR가 강사 부여
    public static UserCourseRole createInstructor(User user, Long courseId) {
        UserCourseRole ucr = new UserCourseRole();
        ucr.user = user;
        ucr.courseId = courseId;
        ucr.role = CourseRole.INSTRUCTOR;
        return ucr;
    }
}
