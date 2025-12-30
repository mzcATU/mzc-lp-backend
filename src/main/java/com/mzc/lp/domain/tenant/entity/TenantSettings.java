package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테넌트 설정 엔티티
 * 브랜딩, 사용자 관리, 기능 활성화 등의 설정을 관리
 */
@Entity
@Table(name = "tenant_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantSettings extends BaseTimeEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    // ============================================
    // 브랜딩 설정
    // ============================================

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String faviconUrl;

    @Column(length = 7)
    private String primaryColor;

    @Column(length = 7)
    private String secondaryColor;

    @Column(length = 100)
    private String fontFamily;

    // ============================================
    // 일반 설정
    // ============================================

    @Column(length = 10, nullable = false)
    private String defaultLanguage = "ko";

    @Column(length = 50, nullable = false)
    private String timezone = "Asia/Seoul";

    // ============================================
    // 사용자 관리 설정
    // ============================================

    @Column(nullable = false)
    private Boolean allowSelfRegistration = true;

    @Column(nullable = false)
    private Boolean requireEmailVerification = true;

    @Column(nullable = false)
    private Boolean requireApproval = false;

    @Column(length = 500)
    private String allowedEmailDomains;

    // ============================================
    // 제한 설정
    // ============================================

    @Column(nullable = false)
    private Integer maxUsersCount = 100;

    @Column(nullable = false)
    private Integer maxStorageGB = 10;

    @Column(nullable = false)
    private Integer maxCourses = 50;

    // ============================================
    // 기능 활성화 설정
    // ============================================

    @Column(nullable = false)
    private Boolean allowCustomDomain = false;

    @Column(nullable = false)
    private Boolean allowCustomBranding = false;

    @Column(nullable = false)
    private Boolean ssoEnabled = false;

    @Column(nullable = false)
    private Boolean apiAccessEnabled = false;

    // 정적 팩토리 메서드
    public static TenantSettings createDefault(Tenant tenant) {
        TenantSettings settings = new TenantSettings();
        settings.tenant = tenant;
        settings.primaryColor = "#3B82F6";
        settings.secondaryColor = "#1E40AF";
        return settings;
    }

    // 브랜딩 업데이트
    public void updateBranding(String logoUrl, String faviconUrl,
                               String primaryColor, String secondaryColor, String fontFamily) {
        this.logoUrl = logoUrl;
        this.faviconUrl = faviconUrl;
        if (primaryColor != null) this.primaryColor = primaryColor;
        if (secondaryColor != null) this.secondaryColor = secondaryColor;
        this.fontFamily = fontFamily;
    }

    // 일반 설정 업데이트
    public void updateGeneralSettings(String defaultLanguage, String timezone) {
        if (defaultLanguage != null) this.defaultLanguage = defaultLanguage;
        if (timezone != null) this.timezone = timezone;
    }

    // 사용자 관리 설정 업데이트
    public void updateUserManagementSettings(Boolean allowSelfRegistration,
                                             Boolean requireEmailVerification,
                                             Boolean requireApproval,
                                             String allowedEmailDomains) {
        if (allowSelfRegistration != null) this.allowSelfRegistration = allowSelfRegistration;
        if (requireEmailVerification != null) this.requireEmailVerification = requireEmailVerification;
        if (requireApproval != null) this.requireApproval = requireApproval;
        this.allowedEmailDomains = allowedEmailDomains;
    }

    // 제한 설정 업데이트
    public void updateLimits(Integer maxUsersCount, Integer maxStorageGB, Integer maxCourses) {
        if (maxUsersCount != null) this.maxUsersCount = maxUsersCount;
        if (maxStorageGB != null) this.maxStorageGB = maxStorageGB;
        if (maxCourses != null) this.maxCourses = maxCourses;
    }

    // 기능 활성화 설정 업데이트
    public void updateFeatures(Boolean allowCustomDomain, Boolean allowCustomBranding,
                               Boolean ssoEnabled, Boolean apiAccessEnabled) {
        if (allowCustomDomain != null) this.allowCustomDomain = allowCustomDomain;
        if (allowCustomBranding != null) this.allowCustomBranding = allowCustomBranding;
        if (ssoEnabled != null) this.ssoEnabled = ssoEnabled;
        if (apiAccessEnabled != null) this.apiAccessEnabled = apiAccessEnabled;
    }
}
