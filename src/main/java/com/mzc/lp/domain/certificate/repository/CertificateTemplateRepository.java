package com.mzc.lp.domain.certificate.repository;

import com.mzc.lp.domain.certificate.entity.CertificateTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {

    Optional<CertificateTemplate> findByIdAndTenantId(Long id, Long tenantId);

    Optional<CertificateTemplate> findByTemplateCodeAndTenantId(String templateCode, Long tenantId);

    Page<CertificateTemplate> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<CertificateTemplate> findByTenantIdAndIsActiveOrderByCreatedAtDesc(Long tenantId, boolean isActive, Pageable pageable);

    List<CertificateTemplate> findByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<CertificateTemplate> findByTenantIdAndIsDefaultTrue(Long tenantId);

    boolean existsByTemplateCodeAndTenantId(String templateCode, Long tenantId);

    @Query("SELECT t FROM CertificateTemplate t WHERE t.tenantId = :tenantId AND t.isDefault = true AND t.isActive = true")
    Optional<CertificateTemplate> findDefaultActiveTemplate(@Param("tenantId") Long tenantId);

    long countByTenantId(Long tenantId);
}
