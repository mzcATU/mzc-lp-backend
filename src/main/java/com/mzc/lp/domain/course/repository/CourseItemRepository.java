package com.mzc.lp.domain.course.repository;

import com.mzc.lp.domain.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {

    Optional<CourseItem> findByIdAndTenantId(Long id, Long tenantId);

    List<CourseItem> findByCourseIdAndTenantId(Long courseId, Long tenantId);

    List<CourseItem> findByCourseIdAndTenantIdAndParentIsNull(Long courseId, Long tenantId);

    List<CourseItem> findByCourseIdAndTenantIdAndParentId(Long courseId, Long tenantId, Long parentId);

    @Query("SELECT ci FROM CourseItem ci WHERE ci.course.id = :courseId AND ci.tenantId = :tenantId ORDER BY ci.depth, ci.sortOrder")
    List<CourseItem> findByCourseIdOrderByDepthAndSortOrder(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT ci FROM CourseItem ci WHERE ci.course.id = :courseId AND ci.tenantId = :tenantId AND ci.learningObjectId IS NOT NULL ORDER BY ci.sortOrder")
    List<CourseItem> findItemsOnlyByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT ci FROM CourseItem ci WHERE ci.course.id = :courseId AND ci.tenantId = :tenantId AND ci.learningObjectId IS NULL ORDER BY ci.depth, ci.sortOrder")
    List<CourseItem> findFoldersOnlyByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    @Query("SELECT ci FROM CourseItem ci LEFT JOIN FETCH ci.children WHERE ci.id = :id AND ci.tenantId = :tenantId")
    Optional<CourseItem> findByIdWithChildren(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT ci FROM CourseItem ci LEFT JOIN FETCH ci.children WHERE ci.course.id = :courseId AND ci.tenantId = :tenantId AND ci.parent IS NULL ORDER BY ci.sortOrder")
    List<CourseItem> findRootItemsWithChildren(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    boolean existsByCourseIdAndTenantIdAndLearningObjectId(Long courseId, Long tenantId, Long learningObjectId);

    long countByCourseIdAndTenantId(Long courseId, Long tenantId);

    long countByCourseIdAndTenantIdAndLearningObjectIdIsNotNull(Long courseId, Long tenantId);
}
