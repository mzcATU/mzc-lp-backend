package com.mzc.lp.domain.certificate.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_certificate_number",
                        columnNames = {"tenant_id", "certificate_number"}
                )
        },
        indexes = {
                @Index(name = "idx_certificate_user", columnList = "tenant_id, user_id"),
                @Index(name = "idx_certificate_status", columnList = "tenant_id, status"),
                @Index(name = "idx_certificate_issued_at", columnList = "tenant_id, issued_at"),
                @Index(name = "idx_certificate_template", columnList = "tenant_id, template_id"),
                @Index(name = "idx_certificate_enrollment", columnList = "tenant_id, enrollment_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends TenantEntity {

    @Version
    private Long version;

    @Column(name = "certificate_number", nullable = false, length = 50)
    private String certificateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private CertificateTemplate template;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    @Column(name = "course_time_title", nullable = false, length = 200)
    private String courseTimeTitle;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateStatus status;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 500)
    private String revokedReason;

    /**
     * 재발급 횟수
     */
    @Column(name = "reissue_count", nullable = false)
    private int reissueCount = 0;

    /**
     * 원본 수료증 ID (재발급인 경우)
     */
    @Column(name = "original_certificate_id")
    private Long originalCertificateId;

    /**
     * 재발급 사유
     */
    @Column(name = "reissue_reason", length = 500)
    private String reissueReason;

    public static Certificate create(
            String certificateNumber,
            Long userId,
            String userName,
            Long enrollmentId,
            Long courseTimeId,
            String courseTimeTitle,
            Instant completedAt
    ) {
        Certificate certificate = new Certificate();
        certificate.certificateNumber = certificateNumber;
        certificate.userId = userId;
        certificate.userName = userName;
        certificate.enrollmentId = enrollmentId;
        certificate.courseTimeId = courseTimeId;
        certificate.courseTimeTitle = courseTimeTitle;
        certificate.completedAt = completedAt;
        certificate.issuedAt = Instant.now();
        certificate.status = CertificateStatus.ISSUED;
        return certificate;
    }

    public static Certificate createWithTemplate(
            String certificateNumber,
            CertificateTemplate template,
            Long userId,
            String userName,
            Long enrollmentId,
            Long courseTimeId,
            String courseTimeTitle,
            Instant completedAt
    ) {
        Certificate certificate = new Certificate();
        certificate.certificateNumber = certificateNumber;
        certificate.template = template;
        certificate.userId = userId;
        certificate.userName = userName;
        certificate.enrollmentId = enrollmentId;
        certificate.courseTimeId = courseTimeId;
        certificate.courseTimeTitle = courseTimeTitle;
        certificate.completedAt = completedAt;
        certificate.issuedAt = Instant.now();
        certificate.status = CertificateStatus.ISSUED;

        // 템플릿에 유효 기간이 설정되어 있으면 만료일 계산
        if (template != null && template.getValidityMonths() != null) {
            certificate.expiresAt = certificate.issuedAt
                    .atZone(java.time.ZoneId.systemDefault())
                    .plusMonths(template.getValidityMonths())
                    .toInstant();
        }

        return certificate;
    }

    /**
     * 재발급 수료증 생성
     */
    public static Certificate createReissue(
            String certificateNumber,
            Certificate original,
            String reissueReason
    ) {
        Certificate certificate = new Certificate();
        certificate.certificateNumber = certificateNumber;
        certificate.template = original.template;
        certificate.userId = original.userId;
        certificate.userName = original.userName;
        certificate.enrollmentId = original.enrollmentId;
        certificate.courseTimeId = original.courseTimeId;
        certificate.courseTimeTitle = original.courseTimeTitle;
        certificate.completedAt = original.completedAt;
        certificate.issuedAt = Instant.now();
        certificate.status = CertificateStatus.ISSUED;
        certificate.reissueCount = original.reissueCount + 1;
        certificate.originalCertificateId = original.getId();
        certificate.reissueReason = reissueReason;

        // 템플릿에 유효 기간이 설정되어 있으면 만료일 계산
        if (original.template != null && original.template.getValidityMonths() != null) {
            certificate.expiresAt = certificate.issuedAt
                    .atZone(java.time.ZoneId.systemDefault())
                    .plusMonths(original.template.getValidityMonths())
                    .toInstant();
        }

        return certificate;
    }

    public void revoke(String reason) {
        this.status = CertificateStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
    }

    /**
     * 재발급으로 인한 무효화
     */
    public void invalidateForReissue(String reason) {
        this.status = CertificateStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedReason = "재발급으로 인한 무효화: " + reason;
    }

    public void expire() {
        this.status = CertificateStatus.EXPIRED;
    }

    public void linkTemplate(CertificateTemplate template) {
        this.template = template;
        if (template != null && template.getValidityMonths() != null) {
            this.expiresAt = this.issuedAt
                    .atZone(java.time.ZoneId.systemDefault())
                    .plusMonths(template.getValidityMonths())
                    .toInstant();
        }
    }

    /**
     * 수료증이 유효한지 확인
     * - 발급됨 또는 유효 상태
     * - 만료되지 않음
     */
    public boolean isValid() {
        if (this.status == CertificateStatus.REVOKED || this.status == CertificateStatus.EXPIRED) {
            return false;
        }
        if (this.expiresAt != null && Instant.now().isAfter(this.expiresAt)) {
            return false;
        }
        return this.status == CertificateStatus.ISSUED || this.status == CertificateStatus.VALID;
    }

    public boolean isRevoked() {
        return this.status == CertificateStatus.REVOKED;
    }

    public boolean isExpired() {
        if (this.status == CertificateStatus.EXPIRED) {
            return true;
        }
        return this.expiresAt != null && Instant.now().isAfter(this.expiresAt);
    }

    /**
     * 만료일이 무기한인지 확인
     */
    public boolean hasUnlimitedValidity() {
        return this.expiresAt == null;
    }
}
