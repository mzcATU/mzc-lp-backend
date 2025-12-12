package com.mzc.lp.domain.course.repository;

import com.mzc.lp.domain.course.entity.CourseRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRelationRepository extends JpaRepository<CourseRelation, Long> {

    Optional<CourseRelation> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT cr FROM CourseRelation cr WHERE cr.toItem.course.id = :courseId AND cr.tenantId = :tenantId")
    List<CourseRelation> findByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT cr FROM CourseRelation cr WHERE cr.fromItem IS NULL AND cr.toItem.course.id = :courseId AND cr.tenantId = :tenantId")
    Optional<CourseRelation> findStartPointByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT cr FROM CourseRelation cr WHERE cr.fromItem.id = :fromItemId AND cr.tenantId = :tenantId")
    Optional<CourseRelation> findByFromItemId(@Param("fromItemId") Long fromItemId, @Param("tenantId") Long tenantId);

    @Query("SELECT cr FROM CourseRelation cr WHERE cr.toItem.id = :toItemId AND cr.tenantId = :tenantId")
    Optional<CourseRelation> findByToItemId(@Param("toItemId") Long toItemId, @Param("tenantId") Long tenantId);

    @Query("SELECT cr FROM CourseRelation cr LEFT JOIN FETCH cr.fromItem LEFT JOIN FETCH cr.toItem WHERE cr.toItem.course.id = :courseId AND cr.tenantId = :tenantId")
    List<CourseRelation> findByCourseIdWithItems(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    boolean existsByFromItemIdAndToItemIdAndTenantId(Long fromItemId, Long toItemId, Long tenantId);

    @Modifying
    @Query("DELETE FROM CourseRelation cr WHERE cr.toItem.course.id = :courseId AND cr.tenantId = :tenantId")
    int deleteByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("DELETE FROM CourseRelation cr WHERE cr.fromItem.id = :itemId OR cr.toItem.id = :itemId")
    int deleteByItemId(@Param("itemId") Long itemId);

    long countByToItemCourseIdAndTenantId(Long courseId, Long tenantId);
}
