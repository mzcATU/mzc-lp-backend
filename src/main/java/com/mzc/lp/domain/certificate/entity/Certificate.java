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
                @Index(name = "idx_certificate_issued_at", columnList = "tenant_id, issued_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends TenantEntity {

    @Version
    private Long version;

    @Column(name = "certificate_number", nullable = false, length = 50)
    private String certificateNumber;

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

    public void revoke(String reason) {
        this.status = CertificateStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
    }

    public boolean isValid() {
        return this.status == CertificateStatus.ISSUED;
    }

    public boolean isRevoked() {
        return this.status == CertificateStatus.REVOKED;
    }
}
