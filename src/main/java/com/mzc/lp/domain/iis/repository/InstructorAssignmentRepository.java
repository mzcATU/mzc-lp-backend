package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstructorAssignmentRepository extends JpaRepository<InstructorAssignment, Long> {

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
}
