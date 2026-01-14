package com.mzc.lp.domain.ts.repository;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.entity.CourseTime;
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
     * Public API용 복합 Specification
     */
    public static Specification<CourseTime> forPublicCatalog(
            Long tenantId,
            List<CourseTimeStatus> statuses,
            DeliveryType deliveryType,
            Long courseId,
            Boolean isFree,
            String keyword,
            Long categoryId
    ) {
        return Specification.where(withTenantId(tenantId))
                .and(withStatusIn(statuses))
                .and(withDeliveryType(deliveryType))
                .and(withCourseId(courseId))
                .and(withIsFree(isFree))
                .and(withKeyword(keyword))
                .and(withCategoryId(categoryId));
    }
}
