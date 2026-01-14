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

    Optional<CourseReview> findByCourseTimeIdAndUserIdAndTenantId(Long courseTimeId, Long userId, Long tenantId);

    Page<CourseReview> findByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId, Pageable pageable);

    boolean existsByCourseTimeIdAndUserIdAndTenantId(Long courseTimeId, Long userId, Long tenantId);

    long countByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.courseTimeId = :courseTimeId AND r.tenantId = :tenantId")
    Double findAverageRatingByCourseTimeId(@Param("courseTimeId") Long courseTimeId, @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(r), AVG(r.rating) FROM CourseReview r WHERE r.courseTimeId = :courseTimeId AND r.tenantId = :tenantId")
    Object[] findReviewStatsForCourseTime(@Param("courseTimeId") Long courseTimeId, @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(r), AVG(r.rating) FROM CourseReview r " +
            "JOIN com.mzc.lp.domain.ts.entity.CourseTime ct ON r.courseTimeId = ct.id " +
            "WHERE ct.course.id = :courseId AND r.tenantId = :tenantId")
    Object[] findReviewStatsForCourse(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);
}
