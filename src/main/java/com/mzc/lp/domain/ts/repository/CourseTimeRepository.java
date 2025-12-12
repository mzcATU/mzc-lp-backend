package com.mzc.lp.domain.ts.repository;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.entity.CourseTime;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseTimeRepository extends JpaRepository<CourseTime, Long> {

    Optional<CourseTime> findByIdAndTenantId(Long id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ct FROM CourseTime ct WHERE ct.id = :id")
    Optional<CourseTime> findByIdWithLock(@Param("id") Long id);

    Page<CourseTime> findByTenantId(Long tenantId, Pageable pageable);

    Page<CourseTime> findByTenantIdAndStatus(Long tenantId, CourseTimeStatus status, Pageable pageable);

    List<CourseTime> findByCmCourseIdAndTenantId(Long cmCourseId, Long tenantId);

    @Query("SELECT ct FROM CourseTime ct WHERE ct.tenantId = :tenantId AND ct.status IN :statuses")
    Page<CourseTime> findByTenantIdAndStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<CourseTimeStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT ct FROM CourseTime ct WHERE ct.cmCourseId = :cmCourseId AND ct.tenantId = :tenantId AND ct.status IN :statuses")
    List<CourseTime> findByCmCourseIdAndTenantIdAndStatusIn(
            @Param("cmCourseId") Long cmCourseId,
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<CourseTimeStatus> statuses
    );

    boolean existsByCmCourseIdAndTenantIdAndStatus(Long cmCourseId, Long tenantId, CourseTimeStatus status);
}
