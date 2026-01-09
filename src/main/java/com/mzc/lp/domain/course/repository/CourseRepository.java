package com.mzc.lp.domain.course.repository;

import com.mzc.lp.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByIdAndTenantId(Long id, Long tenantId);

    Page<Course> findByTenantId(Long tenantId, Pageable pageable);

    Page<Course> findByTenantIdAndTitleContaining(Long tenantId, String keyword, Pageable pageable);

    Page<Course> findByTenantIdAndCategoryId(Long tenantId, Long categoryId, Pageable pageable);

    Page<Course> findByTenantIdAndTitleContainingAndCategoryId(Long tenantId, String keyword, Long categoryId, Pageable pageable);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.items WHERE c.id = :id AND c.tenantId = :tenantId")
    Optional<Course> findByIdWithItems(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    Page<Course> findByTenantIdAndCreatedBy(Long tenantId, Long createdBy, Pageable pageable);

    @Query("SELECT c.id, COUNT(i) FROM Course c LEFT JOIN c.items i WHERE c.id IN :courseIds GROUP BY c.id")
    List<Object[]> countItemsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT COUNT(*) FROM cm_courses WHERE tenant_id = :tenantId", nativeQuery = true)
    long countByTenantId(@Param("tenantId") Long tenantId);
}
