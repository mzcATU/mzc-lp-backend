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

    Page<Course> findByTenantIdAndInstructorId(Long tenantId, Long instructorId, Pageable pageable);

    Page<Course> findByTenantIdAndCategoryId(Long tenantId, Long categoryId, Pageable pageable);

    List<Course> findByTenantIdAndInstructorId(Long tenantId, Long instructorId);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.items WHERE c.id = :id AND c.tenantId = :tenantId")
    Optional<Course> findByIdWithItems(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    long countByTenantIdAndInstructorId(Long tenantId, Long instructorId);
}
