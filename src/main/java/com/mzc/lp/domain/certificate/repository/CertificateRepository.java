package com.mzc.lp.domain.certificate.repository;

import com.mzc.lp.domain.certificate.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Page<Certificate> findByUserIdAndTenantIdOrderByIssuedAtDesc(Long userId, Long tenantId, Pageable pageable);

    boolean existsByEnrollmentIdAndTenantId(Long enrollmentId, Long tenantId);

    Long countByTenantIdAndCertificateNumberStartingWith(Long tenantId, String prefix);
}
