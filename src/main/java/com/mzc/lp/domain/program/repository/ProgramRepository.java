package com.mzc.lp.domain.program.repository;

import com.mzc.lp.common.dto.stats.ProgramStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByIdAndTenantId(Long id, Long tenantId);

    Page<Program> findByTenantId(Long tenantId, Pageable pageable);

    Page<Program> findByTenantIdAndStatus(Long tenantId, ProgramStatus status, Pageable pageable);

    Page<Program> findByTenantIdAndCreatedBy(Long tenantId, Long createdBy, Pageable pageable);

    Page<Program> findByTenantIdAndStatusAndCreatedBy(Long tenantId, ProgramStatus status, Long createdBy, Pageable pageable);

    @Query("SELECT p FROM Program p WHERE p.tenantId = :tenantId AND p.status = 'PENDING' ORDER BY p.submittedAt ASC")
    Page<Program> findPendingPrograms(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Program p WHERE p.tenantId = :tenantId AND p.status = 'PENDING'")
    long countPendingPrograms(@Param("tenantId") Long tenantId);

    @Query("SELECT p FROM Program p LEFT JOIN FETCH p.snapshot WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Program> findByIdWithSnapshot(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    // ===== 통계 집계 쿼리 =====

    /**
     * 테넌트별 상태별 프로그램 카운트
     */
    @Query("SELECT p.status AS status, COUNT(p) AS count " +
            "FROM Program p " +
            "WHERE p.tenantId = :tenantId " +
            "GROUP BY p.status")
    List<StatusCountProjection> countByTenantIdGroupByStatus(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 전체 프로그램 카운트
     */
    long countByTenantId(Long tenantId);

    // ===== OWNER 통계 쿼리 =====

    /**
     * 소유자별 프로그램 카운트
     */
    long countByCreatedByAndTenantId(Long createdBy, Long tenantId);

    /**
     * 소유자별 프로그램 ID 목록 조회
     */
    @Query("SELECT p.id FROM Program p " +
            "WHERE p.createdBy = :createdBy " +
            "AND p.tenantId = :tenantId")
    List<Long> findIdsByCreatedByAndTenantId(
            @Param("createdBy") Long createdBy,
            @Param("tenantId") Long tenantId);

    /**
     * 소유자별 프로그램 통계 (프로그램별 차수 수, 수강생 수, 수료율)
     */
    @Query("SELECT p.id AS programId, p.title AS title, " +
            "COUNT(DISTINCT ct.id) AS courseTimeCount, " +
            "COUNT(e.id) AS totalStudents, " +
            "COUNT(CASE WHEN e.status = 'COMPLETED' THEN 1 END) * 100.0 / NULLIF(COUNT(e.id), 0) AS completionRate " +
            "FROM Program p " +
            "LEFT JOIN CourseTime ct ON ct.program.id = p.id AND ct.tenantId = p.tenantId " +
            "LEFT JOIN Enrollment e ON e.courseTimeId = ct.id AND e.tenantId = p.tenantId " +
            "WHERE p.createdBy = :createdBy " +
            "AND p.tenantId = :tenantId " +
            "GROUP BY p.id, p.title " +
            "ORDER BY p.id")
    List<ProgramStatsProjection> findProgramStatsByOwner(
            @Param("createdBy") Long createdBy,
            @Param("tenantId") Long tenantId);
}
