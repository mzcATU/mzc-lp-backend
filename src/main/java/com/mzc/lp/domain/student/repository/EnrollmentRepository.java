package com.mzc.lp.domain.student.repository;

import com.mzc.lp.common.dto.stats.DailyCountProjection;
import com.mzc.lp.common.dto.stats.MonthlyCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.entity.Enrollment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 기본 조회
    Optional<Enrollment> findByIdAndTenantId(Long id, Long tenantId);

    // 비관적 락 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollment e WHERE e.id = :id")
    Optional<Enrollment> findByIdWithLock(@Param("id") Long id);

    // 중복 체크
    boolean existsByUserIdAndCourseTimeIdAndTenantId(Long userId, Long courseTimeId, Long tenantId);

    // 사용자 + 차수 조회
    Optional<Enrollment> findByUserIdAndCourseTimeIdAndTenantId(Long userId, Long courseTimeId, Long tenantId);

    // 차수별 수강생 목록
    Page<Enrollment> findByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId, Pageable pageable);

    // 차수별 수강생 목록 (상태 필터)
    Page<Enrollment> findByCourseTimeIdAndStatusAndTenantId(
            Long courseTimeId, EnrollmentStatus status, Long tenantId, Pageable pageable);

    // 차수별 수강생 목록 (상태 제외 - 취소 제외 등)
    @Query("SELECT e FROM Enrollment e WHERE e.courseTimeId = :courseTimeId AND e.tenantId = :tenantId AND e.status != :excludeStatus")
    Page<Enrollment> findByCourseTimeIdAndStatusNot(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId,
            @Param("excludeStatus") EnrollmentStatus excludeStatus,
            Pageable pageable);

    // 사용자별 수강 이력
    Page<Enrollment> findByUserIdAndTenantId(Long userId, Long tenantId, Pageable pageable);

    // 사용자별 수강 이력 (상태 필터)
    Page<Enrollment> findByUserIdAndStatusAndTenantId(
            Long userId, EnrollmentStatus status, Long tenantId, Pageable pageable);

    // 사용자별 전체 수강 목록 (정렬용)
    List<Enrollment> findByUserIdAndTenantIdOrderByEnrolledAtDesc(Long userId, Long tenantId);

    // 차수별 정원 카운트 (취소 제외)
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseTimeId = :courseTimeId AND e.tenantId = :tenantId AND e.status != 'DROPPED'")
    long countActiveByCourseTimeId(@Param("courseTimeId") Long courseTimeId, @Param("tenantId") Long tenantId);

    // 차수별 상태별 카운트
    long countByCourseTimeIdAndStatusAndTenantId(Long courseTimeId, EnrollmentStatus status, Long tenantId);

    // 사용자별 상태별 카운트
    long countByUserIdAndStatusAndTenantId(Long userId, EnrollmentStatus status, Long tenantId);

    // 사용자별 전체 카운트
    long countByUserIdAndTenantId(Long userId, Long tenantId);

    // ==================== 통계 조회 ====================

    // 차수별 평균 진도율
    @Query("SELECT AVG(e.progressPercent) FROM Enrollment e WHERE e.courseTimeId = :courseTimeId AND e.tenantId = :tenantId")
    Double findAverageProgressByCourseTimeId(@Param("courseTimeId") Long courseTimeId, @Param("tenantId") Long tenantId);

    // 차수별 전체 카운트
    long countByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId);

    // 사용자별 평균 진도율
    @Query("SELECT AVG(e.progressPercent) FROM Enrollment e WHERE e.userId = :userId AND e.tenantId = :tenantId")
    Double findAverageProgressByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    // 사용자별 평균 점수 (수료한 과정만)
    @Query("SELECT AVG(e.score) FROM Enrollment e WHERE e.userId = :userId AND e.tenantId = :tenantId AND e.status = 'COMPLETED' AND e.score IS NOT NULL")
    Double findAverageScoreByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    // ==================== 통계 집계 쿼리 ====================

    /**
     * 테넌트별 상태별 수강 카운트
     */
    @Query("SELECT e.status AS status, COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId " +
            "GROUP BY e.status")
    List<StatusCountProjection> countByTenantIdGroupByStatus(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 수강 유형별 카운트 (VOLUNTARY/MANDATORY)
     */
    @Query("SELECT e.type AS type, COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId " +
            "GROUP BY e.type")
    List<TypeCountProjection> countByTenantIdGroupByType(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 전체 수강 카운트
     */
    long countByTenantId(Long tenantId);

    /**
     * 테넌트별 일별 수강신청 카운트 (기간 내)
     */
    @Query("SELECT FUNCTION('DATE', e.enrolledAt) AS date, COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId " +
            "AND e.enrolledAt >= :startDate " +
            "AND e.enrolledAt < :endDate " +
            "GROUP BY FUNCTION('DATE', e.enrolledAt) " +
            "ORDER BY FUNCTION('DATE', e.enrolledAt)")
    List<DailyCountProjection> countDailyEnrollments(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 월별 수강신청 카운트 (기간 내)
     */
    @Query("SELECT FUNCTION('YEAR', e.enrolledAt) AS year, " +
            "FUNCTION('MONTH', e.enrolledAt) AS month, " +
            "COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId " +
            "AND e.enrolledAt >= :startDate " +
            "AND e.enrolledAt < :endDate " +
            "GROUP BY FUNCTION('YEAR', e.enrolledAt), FUNCTION('MONTH', e.enrolledAt) " +
            "ORDER BY FUNCTION('YEAR', e.enrolledAt), FUNCTION('MONTH', e.enrolledAt)")
    List<MonthlyCountProjection> countMonthlyEnrollments(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 평균 진도율
     */
    @Query("SELECT AVG(e.progressPercent) FROM Enrollment e WHERE e.tenantId = :tenantId")
    Double findAverageProgressByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 평균 점수 (수료한 과정만)
     */
    @Query("SELECT AVG(e.score) FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId " +
            "AND e.status = 'COMPLETED' " +
            "AND e.score IS NOT NULL")
    Double findAverageScoreByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 수료율 (COMPLETED / 전체 * 100)
     */
    @Query("SELECT COUNT(CASE WHEN e.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(e) " +
            "FROM Enrollment e " +
            "WHERE e.tenantId = :tenantId")
    Double getCompletionRateByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 차수별 수강 유형별 카운트
     */
    @Query("SELECT e.type AS type, COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.courseTimeId = :courseTimeId " +
            "AND e.tenantId = :tenantId " +
            "GROUP BY e.type")
    List<TypeCountProjection> countByCourseTimeIdGroupByType(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId);

    /**
     * 차수별 상태별 카운트 (GROUP BY)
     */
    @Query("SELECT e.status AS status, COUNT(e) AS count " +
            "FROM Enrollment e " +
            "WHERE e.courseTimeId = :courseTimeId " +
            "AND e.tenantId = :tenantId " +
            "GROUP BY e.status")
    List<StatusCountProjection> countByCourseTimeIdGroupByStatus(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId);
}
