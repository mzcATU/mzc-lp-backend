package com.mzc.lp.domain.certificate.repository;

import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Page<Certificate> findByUserIdAndTenantIdOrderByIssuedAtDesc(Long userId, Long tenantId, Pageable pageable);

    boolean existsByEnrollmentIdAndTenantId(Long enrollmentId, Long tenantId);

    /**
     * 해당 수강에 대해 유효한(발급됨) 수료증이 존재하는지 확인
     */
    @Query("SELECT COUNT(c) > 0 FROM Certificate c WHERE c.enrollmentId = :enrollmentId AND c.tenantId = :tenantId AND c.status = :status")
    boolean existsByEnrollmentIdAndTenantIdAndStatus(
            @Param("enrollmentId") Long enrollmentId,
            @Param("tenantId") Long tenantId,
            @Param("status") CertificateStatus status
    );

    /**
     * 해당 수강에 대한 유효한 수료증 조회
     */
    @Query("SELECT c FROM Certificate c WHERE c.enrollmentId = :enrollmentId AND c.tenantId = :tenantId AND c.status = :status")
    Optional<Certificate> findByEnrollmentIdAndTenantIdAndStatus(
            @Param("enrollmentId") Long enrollmentId,
            @Param("tenantId") Long tenantId,
            @Param("status") CertificateStatus status
    );

    Long countByTenantIdAndCertificateNumberStartingWith(Long tenantId, String prefix);
}
