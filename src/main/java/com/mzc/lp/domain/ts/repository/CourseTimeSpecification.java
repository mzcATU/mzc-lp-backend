package com.mzc.lp.domain.ts.repository;

import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * CourseTime 동적 쿼리를 위한 Specification
 */
public class CourseTimeSpecification {

    private CourseTimeSpecification() {
        // Utility class
    }

    public static Specification<CourseTime> withTenantId(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<CourseTime> withStatusIn(List<CourseTimeStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<CourseTime> withDeliveryType(DeliveryType deliveryType) {
        return (root, query, cb) -> {
            if (deliveryType == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("deliveryType"), deliveryType);
        };
    }

    public static Specification<CourseTime> withCourseId(Long courseId) {
        return (root, query, cb) -> {
            if (courseId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("course").get("id"), courseId);
        };
    }

    public static Specification<CourseTime> withIsFree(Boolean isFree) {
        return (root, query, cb) -> {
            if (isFree == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("free"), isFree);
        };
    }

    public static Specification<CourseTime> withKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), pattern);
        };
    }

    public static Specification<CourseTime> withCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            // CourseTime → Course.categoryId
            return cb.equal(root.get("course").get("categoryId"), categoryId);
        };
    }

    /**
     * INVITE_ONLY 과정 필터링
     * - INVITE_ONLY가 아닌 과정: 그대로 반환
     * - INVITE_ONLY 과정: 해당 사용자의 enrollment가 존재하는 경우에만 반환
     *
     * @param userId 현재 사용자 ID (null인 경우 INVITE_ONLY 제외)
     * @param tenantId 테넌트 ID
     */
    public static Specification<CourseTime> withInviteOnlyFilter(Long userId, Long tenantId) {
        return (root, query, cb) -> {
            // INVITE_ONLY가 아닌 과정은 모두 포함
            var notInviteOnly = cb.notEqual(root.get("enrollmentMethod"), EnrollmentMethod.INVITE_ONLY);

            if (userId == null) {
                // 비로그인 사용자: INVITE_ONLY 제외
                return notInviteOnly;
            }

            // INVITE_ONLY이면서 enrollment가 존재하는 경우
            Subquery<Long> enrollmentSubquery = query.subquery(Long.class);
            var enrollmentRoot = enrollmentSubquery.from(Enrollment.class);
            enrollmentSubquery.select(enrollmentRoot.get("id"))
                    .where(
                            cb.equal(enrollmentRoot.get("courseTimeId"), root.get("id")),
                            cb.equal(enrollmentRoot.get("userId"), userId),
                            cb.equal(enrollmentRoot.get("tenantId"), tenantId)
                    );

            var inviteOnlyWithEnrollment = cb.and(
                    cb.equal(root.get("enrollmentMethod"), EnrollmentMethod.INVITE_ONLY),
                    cb.exists(enrollmentSubquery)
            );

            // INVITE_ONLY가 아니거나, INVITE_ONLY이면서 enrollment가 존재하는 경우
            return cb.or(notInviteOnly, inviteOnlyWithEnrollment);
        };
    }

    /**
     * Public API용 복합 Specification
     *
     * @param userId 현재 사용자 ID (INVITE_ONLY 필터링용, null 가능)
     */
    public static Specification<CourseTime> forPublicCatalog(
            Long tenantId,
            List<CourseTimeStatus> statuses,
            DeliveryType deliveryType,
            Long courseId,
            Boolean isFree,
            String keyword,
            Long categoryId,
            Long userId
    ) {
        return Specification.where(withTenantId(tenantId))
                .and(withStatusIn(statuses))
                .and(withDeliveryType(deliveryType))
                .and(withCourseId(courseId))
                .and(withIsFree(isFree))
                .and(withKeyword(keyword))
                .and(withCategoryId(categoryId))
                .and(withInviteOnlyFilter(userId, tenantId));
    }
}
