package com.mzc.lp.domain.course.repository;

import com.mzc.lp.domain.course.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByIdAndTenantId(Long id, Long tenantId);

    Optional<CourseReview> findByCourseIdAndUserIdAndTenantId(Long courseId, Long userId, Long tenantId);

    Page<CourseReview> findByCourseIdAndTenantId(Long courseId, Long tenantId, Pageable pageable);

    boolean existsByCourseIdAndUserIdAndTenantId(Long courseId, Long userId, Long tenantId);

    long countByCourseIdAndTenantId(Long courseId, Long tenantId);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.courseId = :courseId AND r.tenantId = :tenantId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(r), AVG(r.rating) FROM CourseReview r WHERE r.courseId = :courseId AND r.tenantId = :tenantId")
    Object[] findReviewStatsForCourse(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);
}
