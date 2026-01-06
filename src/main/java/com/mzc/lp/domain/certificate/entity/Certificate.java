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
                ),
                @UniqueConstraint(
                        name = "uk_certificate_enrollment",
                        columnNames = {"tenant_id", "enrollment_id"}
                )
        },
        indexes = {
                @Index(name = "idx_certificate_user", columnList = "tenant_id, user_id"),
                @Index(name = "idx_certificate_status", columnList = "tenant_id, status"),
                @Index(name = "idx_certificate_issued_at", columnList = "tenant_id, issued_at"),
                @Index(name = "idx_certificate_template", columnList = "tenant_id, template_id")
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

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "program_title", length = 255)
    private String programTitle;

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

    public static Certificate create(
            String certificateNumber,
            Long userId,
            String userName,
            Long enrollmentId,
            Long courseTimeId,
            String courseTimeTitle,
            Long programId,
            String programTitle,
            Instant completedAt
    ) {
        Certificate certificate = new Certificate();
        certificate.certificateNumber = certificateNumber;
        certificate.userId = userId;
        certificate.userName = userName;
        certificate.enrollmentId = enrollmentId;
        certificate.courseTimeId = courseTimeId;
        certificate.courseTimeTitle = courseTimeTitle;
        certificate.programId = programId;
        certificate.programTitle = programTitle;
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
            Long programId,
            String programTitle,
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
        certificate.programId = programId;
        certificate.programTitle = programTitle;
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

    public void revoke(String reason) {
        this.status = CertificateStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
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
