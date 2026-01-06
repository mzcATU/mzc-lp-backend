package com.mzc.lp.domain.certificate.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수료증 템플릿 엔티티
 * 수료증의 디자인 및 레이아웃 정보를 관리
 */
@Entity
@Table(name = "certificate_templates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_certificate_template_code",
                        columnNames = {"tenant_id", "template_code"}
                )
        },
        indexes = {
                @Index(name = "idx_certificate_template_active", columnList = "tenant_id, is_active"),
                @Index(name = "idx_certificate_template_default", columnList = "tenant_id, is_default")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CertificateTemplate extends TenantEntity {

    @Version
    private Long version;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "background_image_url", length = 500)
    private String backgroundImageUrl;

    @Column(name = "signature_image_url", length = 500)
    private String signatureImageUrl;

    @Column(name = "certificate_body_html", columnDefinition = "TEXT")
    private String certificateBodyHtml;

    /**
     * 유효 기간 (월 단위)
     * null이면 무기한 유효
     */
    @Column(name = "validity_months")
    private Integer validityMonths;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_by")
    private Long createdBy;

    // ===== 정적 팩토리 메서드 =====
    public static CertificateTemplate create(
            String templateCode,
            String title,
            String description,
            Long createdBy
    ) {
        CertificateTemplate template = new CertificateTemplate();
        template.templateCode = templateCode;
        template.title = title;
        template.description = description;
        template.createdBy = createdBy;
        template.isDefault = false;
        template.isActive = true;
        return template;
    }

    public static CertificateTemplate createDefault(
            String templateCode,
            String title,
            String certificateBodyHtml,
            Long createdBy
    ) {
        CertificateTemplate template = new CertificateTemplate();
        template.templateCode = templateCode;
        template.title = title;
        template.certificateBodyHtml = certificateBodyHtml;
        template.createdBy = createdBy;
        template.isDefault = true;
        template.isActive = true;
        return template;
    }

    // ===== 비즈니스 메서드 =====
    public void update(
            String title,
            String description,
            String backgroundImageUrl,
            String signatureImageUrl,
            String certificateBodyHtml,
            Integer validityMonths
    ) {
        this.title = title;
        this.description = description;
        this.backgroundImageUrl = backgroundImageUrl;
        this.signatureImageUrl = signatureImageUrl;
        this.certificateBodyHtml = certificateBodyHtml;
        this.validityMonths = validityMonths;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 유효 기간이 무기한인지 확인
     */
    public boolean hasUnlimitedValidity() {
        return this.validityMonths == null;
    }
}
