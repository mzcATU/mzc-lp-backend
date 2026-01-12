package com.mzc.lp.domain.ts.repository;

import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.entity.CourseTime;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CourseTimeRepository extends JpaRepository<CourseTime, Long>, JpaSpecificationExecutor<CourseTime> {

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

    // ===== IIS 충돌 검사용 =====

    /**
     * 특정 기간과 겹치는 CourseTime 조회
     * 기간 겹침 조건: NOT (ct.classEndDate < startDate OR ct.classStartDate > endDate)
     * = ct.classEndDate >= startDate AND ct.classStartDate <= endDate
     */
    @Query("SELECT ct FROM CourseTime ct " +
            "WHERE ct.id IN :timeIds " +
            "AND ct.classStartDate <= :endDate " +
            "AND ct.classEndDate >= :startDate")
    List<CourseTime> findByIdInAndDateRangeOverlap(
            @Param("timeIds") List<Long> timeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===== 통계 집계 쿼리 =====

    /**
     * 테넌트별 상태별 차수 카운트
     */
    @Query("SELECT ct.status AS status, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "GROUP BY ct.status")
    List<StatusCountProjection> countByTenantIdGroupByStatus(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 운영 방식별 차수 카운트
     */
    @Query("SELECT ct.deliveryType AS type, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "GROUP BY ct.deliveryType")
    List<TypeCountProjection> countByTenantIdGroupByDeliveryType(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 무료/유료 차수 카운트
     */
    @Query("SELECT ct.free AS value, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "GROUP BY ct.free")
    List<BooleanCountProjection> countByTenantIdGroupByFree(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 전체 차수 카운트
     */
    long countByTenantId(Long tenantId);

    /**
     * 테넌트별 평균 정원 활용률 (currentEnrollment / capacity * 100)
     * capacity가 0인 경우 제외
     */
    @Query("SELECT AVG(ct.currentEnrollment * 100.0 / ct.capacity) " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.capacity > 0")
    Double getAverageCapacityUtilization(@Param("tenantId") Long tenantId);

    // ===== 기간 필터 통계 쿼리 (TO 대시보드) =====

    /**
     * 테넌트별 상태별 차수 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT ct.status AS status, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate " +
            "GROUP BY ct.status")
    List<StatusCountProjection> countByTenantIdGroupByStatusWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 운영 방식별 차수 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT ct.deliveryType AS type, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate " +
            "GROUP BY ct.deliveryType")
    List<TypeCountProjection> countByTenantIdGroupByDeliveryTypeWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 무료/유료 차수 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT ct.free AS value, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate " +
            "GROUP BY ct.free")
    List<BooleanCountProjection> countByTenantIdGroupByFreeWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 전체 차수 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT COUNT(ct) FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate")
    long countByTenantIdWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 평균 정원 활용률 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT AVG(ct.currentEnrollment * 100.0 / ct.capacity) " +
            "FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.capacity > 0 " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate")
    Double getAverageCapacityUtilizationWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 강사 미배정 차수 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT COUNT(ct) FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.status IN :statuses " +
            "AND ct.createdAt >= :startDate AND ct.createdAt < :endDate " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM InstructorAssignment ia " +
            "    WHERE ia.timeKey = ct.id " +
            "    AND ia.tenantId = :tenantId " +
            "    AND ia.role = :role " +
            "    AND ia.status = :assignmentStatus" +
            ")")
    long countCourseTimesNeedingInstructorWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<CourseTimeStatus> statuses,
            @Param("role") InstructorRole role,
            @Param("assignmentStatus") AssignmentStatus assignmentStatus,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 과정(cmCourseId)별 상태별 차수 카운트
     */
    @Query("SELECT ct.status AS status, COUNT(ct) AS count " +
            "FROM CourseTime ct " +
            "WHERE ct.cmCourseId = :cmCourseId " +
            "AND ct.tenantId = :tenantId " +
            "GROUP BY ct.status")
    List<StatusCountProjection> countByCmCourseIdGroupByStatus(
            @Param("cmCourseId") Long cmCourseId,
            @Param("tenantId") Long tenantId);

    /**
     * 과정(cmCourseId)별 전체 차수 카운트
     */
    long countByCmCourseIdAndTenantId(Long cmCourseId, Long tenantId);

    /**
     * 강사 미배정 차수 카운트 (RECRUITING 또는 ONGOING 상태)
     * 주강사(MAIN)가 ACTIVE 상태로 배정되지 않은 차수
     */
    @Query("SELECT COUNT(ct) FROM CourseTime ct " +
            "WHERE ct.tenantId = :tenantId " +
            "AND ct.status IN :statuses " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM InstructorAssignment ia " +
            "    WHERE ia.timeKey = ct.id " +
            "    AND ia.tenantId = :tenantId " +
            "    AND ia.role = :role " +
            "    AND ia.status = :assignmentStatus" +
            ")")
    long countCourseTimesNeedingInstructor(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<CourseTimeStatus> statuses,
            @Param("role") InstructorRole role,
            @Param("assignmentStatus") AssignmentStatus assignmentStatus);

    // ===== 내 강의 통계 쿼리 =====

    /**
     * 프로그램 ID 목록에 속한 차수 ID 목록 조회
     */
    @Query("SELECT ct.id FROM CourseTime ct " +
            "WHERE ct.program.id IN :programIds " +
            "AND ct.tenantId = :tenantId")
    List<Long> findIdsByProgramIdInAndTenantId(
            @Param("programIds") List<Long> programIds,
            @Param("tenantId") Long tenantId);
}
