package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
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

public interface InstructorAssignmentRepository extends JpaRepository<InstructorAssignment, Long>, InstructorAssignmentRepositoryCustom {

    Optional<InstructorAssignment> findByIdAndTenantId(Long id, Long tenantId);

    // 차수별 강사 목록 조회
    List<InstructorAssignment> findByTimeKeyAndTenantId(Long timeKey, Long tenantId);

    List<InstructorAssignment> findByTimeKeyAndTenantIdAndStatus(
            Long timeKey, Long tenantId, AssignmentStatus status);

    // 강사별 배정 목록 조회
    List<InstructorAssignment> findByUserKeyAndTenantId(Long userKey, Long tenantId);

    Page<InstructorAssignment> findByUserKeyAndTenantId(Long userKey, Long tenantId, Pageable pageable);

    Page<InstructorAssignment> findByUserKeyAndTenantIdAndStatus(
            Long userKey, Long tenantId, AssignmentStatus status, Pageable pageable);

    // 중복 배정 체크
    boolean existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
            Long timeKey, Long userKey, Long tenantId, AssignmentStatus status);

    // 차수의 ACTIVE 주강사 조회
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.timeKey = :timeKey " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.role = :role " +
            "AND ia.status = 'ACTIVE'")
    Optional<InstructorAssignment> findActiveByTimeKeyAndRole(
            @Param("timeKey") Long timeKey,
            @Param("tenantId") Long tenantId,
            @Param("role") InstructorRole role);

    // [Race Condition 방지] 비관적 락으로 ACTIVE 주강사 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.timeKey = :timeKey " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.role = :role " +
            "AND ia.status = 'ACTIVE'")
    Optional<InstructorAssignment> findActiveByTimeKeyAndRoleWithLock(
            @Param("timeKey") Long timeKey,
            @Param("tenantId") Long tenantId,
            @Param("role") InstructorRole role);

    // 차수의 ACTIVE 강사 수 조회
    @Query("SELECT COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.timeKey = :timeKey " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.status = 'ACTIVE'")
    long countActiveByTimeKey(@Param("timeKey") Long timeKey, @Param("tenantId") Long tenantId);

    // 강사별 ACTIVE 배정 수 조회
    @Query("SELECT COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.userKey = :userKey " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.status = 'ACTIVE'")
    long countActiveByUserKey(@Param("userKey") Long userKey, @Param("tenantId") Long tenantId);

    // ========== TS 모듈 연동용 메서드 ==========

    // MAIN 강사 존재 여부 확인
    @Query("SELECT CASE WHEN COUNT(ia) > 0 THEN true ELSE false END " +
            "FROM InstructorAssignment ia " +
            "WHERE ia.timeKey = :timeKey " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.role = 'MAIN' " +
            "AND ia.status = 'ACTIVE'")
    boolean existsActiveMainInstructor(@Param("timeKey") Long timeKey, @Param("tenantId") Long tenantId);

    // 여러 차수의 강사 목록 Bulk 조회 (N+1 방지)
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.timeKey IN :timeKeys " +
            "AND ia.tenantId = :tenantId " +
            "AND ia.status = 'ACTIVE' " +
            "ORDER BY ia.timeKey, ia.role")
    List<InstructorAssignment> findActiveByTimeKeyIn(
            @Param("timeKeys") List<Long> timeKeys,
            @Param("tenantId") Long tenantId);

    // ========== 통계 API용 메서드 ==========

    // 전체 배정 건수
    long countByTenantId(Long tenantId);

    // 상태별 배정 건수
    long countByTenantIdAndStatus(Long tenantId, AssignmentStatus status);

    // 역할별 집계
    @Query("SELECT ia.role, COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "GROUP BY ia.role")
    List<Object[]> countGroupByRole(@Param("tenantId") Long tenantId);

    // 상태별 집계
    @Query("SELECT ia.status, COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "GROUP BY ia.status")
    List<Object[]> countGroupByStatus(@Param("tenantId") Long tenantId);

    // 강사별 통계 (ACTIVE 상태만)
    @Query("SELECT ia.userKey, COUNT(ia), " +
            "SUM(CASE WHEN ia.role = 'MAIN' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN ia.role = 'SUB' THEN 1 ELSE 0 END) " +
            "FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId AND ia.status = 'ACTIVE' " +
            "GROUP BY ia.userKey")
    List<Object[]> getInstructorStatistics(@Param("tenantId") Long tenantId);

    // 특정 강사의 통계
    @Query("SELECT COUNT(ia), " +
            "SUM(CASE WHEN ia.role = 'MAIN' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN ia.role = 'SUB' THEN 1 ELSE 0 END) " +
            "FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId AND ia.userKey = :userKey AND ia.status = 'ACTIVE'")
    List<Object[]> getInstructorStatisticsByUserId(@Param("tenantId") Long tenantId, @Param("userKey") Long userKey);

    // ========== 기간 필터링 통계 API용 메서드 ==========

    // 기간별 전체 배정 건수
    @Query("SELECT COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate")
    long countByTenantIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기간별 상태별 배정 건수
    @Query("SELECT COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND ia.status = :status " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate")
    long countByTenantIdAndStatusAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("status") AssignmentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기간별 역할별 집계
    @Query("SELECT ia.role, COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate " +
            "GROUP BY ia.role")
    List<Object[]> countGroupByRoleAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기간별 상태별 집계
    @Query("SELECT ia.status, COUNT(ia) FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate " +
            "GROUP BY ia.status")
    List<Object[]> countGroupByStatusAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기간별 강사별 통계 (ACTIVE 상태만)
    @Query("SELECT ia.userKey, COUNT(ia), " +
            "SUM(CASE WHEN ia.role = 'MAIN' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN ia.role = 'SUB' THEN 1 ELSE 0 END) " +
            "FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId AND ia.status = 'ACTIVE' " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate " +
            "GROUP BY ia.userKey")
    List<Object[]> getInstructorStatisticsWithDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기간별 특정 강사의 통계
    @Query("SELECT COUNT(ia), " +
            "SUM(CASE WHEN ia.role = 'MAIN' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN ia.role = 'SUB' THEN 1 ELSE 0 END) " +
            "FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId AND ia.userKey = :userKey AND ia.status = 'ACTIVE' " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate")
    List<Object[]> getInstructorStatisticsByUserIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("userKey") Long userKey,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 특정 강사의 ACTIVE 배정 목록 조회 (차수별 통계용)
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND ia.userKey = :userKey " +
            "AND ia.status = 'ACTIVE' " +
            "ORDER BY ia.assignedAt DESC")
    List<InstructorAssignment> findActiveByUserKey(
            @Param("tenantId") Long tenantId,
            @Param("userKey") Long userKey);

    // 기간별 특정 강사의 ACTIVE 배정 목록 조회 (차수별 통계용)
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND ia.userKey = :userKey " +
            "AND ia.status = 'ACTIVE' " +
            "AND CAST(ia.assignedAt AS LocalDate) >= :startDate " +
            "AND CAST(ia.assignedAt AS LocalDate) <= :endDate " +
            "ORDER BY ia.assignedAt DESC")
    List<InstructorAssignment> findActiveByUserKeyAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("userKey") Long userKey,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ========== 가용성 확인 API용 메서드 ==========

    // 여러 강사의 ACTIVE 배정 목록 Bulk 조회
    @Query("SELECT ia FROM InstructorAssignment ia " +
            "WHERE ia.tenantId = :tenantId " +
            "AND ia.userKey IN :userKeys " +
            "AND ia.status = 'ACTIVE'")
    List<InstructorAssignment> findActiveByUserKeyIn(
            @Param("tenantId") Long tenantId,
            @Param("userKeys") List<Long> userKeys);
}
