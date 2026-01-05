package com.mzc.lp.domain.system.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테넌트 기본값 설정 엔티티 (SA 관리)
 * 새 테넌트 생성 시 적용되는 기본값을 관리
 * 단일 레코드만 존재 (Singleton)
 */
@Entity
@Table(name = "tenant_defaults")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantDefaults extends BaseTimeEntity {

    @Column(nullable = false, unique = true)
    private String settingsKey = "DEFAULT";

    // ============================================
    // 리소스 제한 기본값
    // ============================================

    @Column(nullable = false)
    private Integer defaultMaxUsers = 100;

    @Column(nullable = false)
    private Integer defaultMaxCourses = 50;

    @Column(nullable = false)
    private Integer defaultMaxStorageGB = 50;

    @Column(nullable = false)
    private Integer defaultMaxAdmins = 5;

    // ============================================
    // 기능 활성화 기본값
    // ============================================

    @Column(nullable = false)
    private Boolean defaultCustomDomain = false;

    @Column(nullable = false)
    private Boolean defaultSsoIntegration = false;

    @Column(nullable = false)
    private Boolean defaultApiAccess = false;

    @Column(nullable = false)
    private Boolean defaultWhiteLabeling = false;

    @Column(nullable = false)
    private Boolean defaultAdvancedAnalytics = false;

    // ============================================
    // 브랜딩 권한 기본값
    // ============================================

    @Column(nullable = false)
    private Boolean defaultAllowCustomLogo = true;

    @Column(nullable = false)
    private Boolean defaultAllowCustomColors = true;

    @Column(nullable = false)
    private Boolean defaultAllowCustomFonts = false;

    // ============================================
    // 알림 설정 기본값
    // ============================================

    @Column(nullable = false)
    private Boolean defaultEmailNotifications = true;

    @Column(nullable = false)
    private Boolean defaultPushNotifications = false;

    @Column(nullable = false)
    private Boolean defaultSmsNotifications = false;

    // 정적 팩토리 메서드
    public static TenantDefaults createDefault() {
        TenantDefaults defaults = new TenantDefaults();
        defaults.settingsKey = "DEFAULT";
        return defaults;
    }

    // 리소스 제한 업데이트
    public void updateLimits(Integer maxUsers, Integer maxCourses,
                             Integer maxStorageGB, Integer maxAdmins) {
        if (maxUsers != null) this.defaultMaxUsers = maxUsers;
        if (maxCourses != null) this.defaultMaxCourses = maxCourses;
        if (maxStorageGB != null) this.defaultMaxStorageGB = maxStorageGB;
        if (maxAdmins != null) this.defaultMaxAdmins = maxAdmins;
    }

    // 기능 활성화 업데이트
    public void updateFeatures(Boolean customDomain, Boolean ssoIntegration,
                               Boolean apiAccess, Boolean whiteLabeling,
                               Boolean advancedAnalytics) {
        if (customDomain != null) this.defaultCustomDomain = customDomain;
        if (ssoIntegration != null) this.defaultSsoIntegration = ssoIntegration;
        if (apiAccess != null) this.defaultApiAccess = apiAccess;
        if (whiteLabeling != null) this.defaultWhiteLabeling = whiteLabeling;
        if (advancedAnalytics != null) this.defaultAdvancedAnalytics = advancedAnalytics;
    }

    // 브랜딩 권한 업데이트
    public void updateBrandingPermissions(Boolean allowCustomLogo, Boolean allowCustomColors,
                                          Boolean allowCustomFonts) {
        if (allowCustomLogo != null) this.defaultAllowCustomLogo = allowCustomLogo;
        if (allowCustomColors != null) this.defaultAllowCustomColors = allowCustomColors;
        if (allowCustomFonts != null) this.defaultAllowCustomFonts = allowCustomFonts;
    }

    // 알림 설정 업데이트
    public void updateNotifications(Boolean emailNotifications, Boolean pushNotifications,
                                    Boolean smsNotifications) {
        if (emailNotifications != null) this.defaultEmailNotifications = emailNotifications;
        if (pushNotifications != null) this.defaultPushNotifications = pushNotifications;
        if (smsNotifications != null) this.defaultSmsNotifications = smsNotifications;
    }
}
