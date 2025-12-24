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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CourseTimeRepository extends JpaRepository<CourseTime, Long> {

    Optional<CourseTime> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

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

    Page<CourseTime> findByCmCourseIdAndTenantId(Long cmCourseId, Long tenantId, Pageable pageable);

    @Query("SELECT ct FROM CourseTime ct WHERE ct.cmCourseId = :cmCourseId AND ct.tenantId = :tenantId AND ct.status = :status")
    Page<CourseTime> findByCmCourseIdAndTenantIdAndStatus(
            @Param("cmCourseId") Long cmCourseId,
            @Param("tenantId") Long tenantId,
            @Param("status") CourseTimeStatus status,
            Pageable pageable
    );

    boolean existsByCmCourseIdAndTenantIdAndStatus(Long cmCourseId, Long tenantId, CourseTimeStatus status);

    // ===== 배치 Job용 쿼리 =====

    /**
     * RECRUITING 상태이면서 classStartDate가 도래한 차수 조회
     * (상시 모집 차수는 classStartDate가 9999-12-31이므로 자연스럽게 제외)
     */
    List<CourseTime> findByStatusAndClassStartDateLessThanEqual(CourseTimeStatus status, LocalDate today);

    /**
     * ONGOING 상태이면서 classEndDate가 경과한 차수 조회
     * (상시 모집 차수는 classEndDate가 9999-12-31이므로 자연스럽게 제외)
     */
    List<CourseTime> findByStatusAndClassEndDateLessThan(CourseTimeStatus status, LocalDate today);
}
