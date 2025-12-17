package com.mzc.lp.domain.program.repository;

import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByIdAndTenantId(Long id, Long tenantId);

    Page<Program> findByTenantId(Long tenantId, Pageable pageable);

    Page<Program> findByTenantIdAndStatus(Long tenantId, ProgramStatus status, Pageable pageable);

    Page<Program> findByTenantIdAndCreatorId(Long tenantId, Long creatorId, Pageable pageable);

    Page<Program> findByTenantIdAndStatusAndCreatorId(Long tenantId, ProgramStatus status, Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Program p WHERE p.tenantId = :tenantId AND p.status = 'PENDING' ORDER BY p.submittedAt ASC")
    Page<Program> findPendingPrograms(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Program p WHERE p.tenantId = :tenantId AND p.status = 'PENDING'")
    long countPendingPrograms(@Param("tenantId") Long tenantId);

    @Query("SELECT p FROM Program p LEFT JOIN FETCH p.snapshot WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Program> findByIdWithSnapshot(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);
}
