package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

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
    private String darkLogoUrl;

    @Column(length = 500)
    private String faviconUrl;

    @Column(length = 7)
    private String primaryColor;

    @Column(length = 7)
    private String secondaryColor;

    @Column(length = 7)
    private String accentColor;

    @Column(length = 100)
    private String fontFamily;

    @Column(length = 100)
    private String headingFont;

    @Column(length = 100)
    private String bodyFont;

    // ============================================
    // 확장 브랜딩 설정
    // ============================================

    @Column(length = 200)
    private String companyName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> bannerSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> landingPageSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> sidebarTUSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> sidebarCOSettings = new HashMap<>();

    // ============================================
    // 레이아웃 설정 (JSON)
    // ============================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> headerSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> sidebarSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> footerSettings = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> contentSettings = new HashMap<>();

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

    // ============================================
    // 테넌트 기능 On/Off 설정
    // ============================================

    @Column(nullable = false)
    private Boolean communityEnabled = true;

    @Column(nullable = false)
    private Boolean userCourseCreationEnabled = false;

    @Column(nullable = false)
    private Boolean cartEnabled = true;

    @Column(nullable = false)
    private Boolean wishlistEnabled = true;

    @Column(nullable = false)
    private Boolean instructorTabEnabled = true;

    @Column(nullable = false)
    private Boolean paidModeEnabled = true; // 유료 모드 (false면 무료 모드 - 가격 숨김)

    // 정적 팩토리 메서드
    public static TenantSettings createDefault(Tenant tenant) {
        TenantSettings settings = new TenantSettings();
        settings.tenant = tenant;
        settings.primaryColor = "#4C2D9A";
        settings.secondaryColor = "#3D2478";
        settings.accentColor = "#10B981";
        settings.headingFont = "Pretendard";
        settings.bodyFont = "Pretendard";

        // 기본 헤더 설정
        settings.headerSettings = new HashMap<>();
        settings.headerSettings.put("style", "fixed");
        settings.headerSettings.put("showLogo", true);
        settings.headerSettings.put("showSearch", true);
        settings.headerSettings.put("showNotifications", true);

        // 기본 사이드바 설정
        settings.sidebarSettings = new HashMap<>();
        settings.sidebarSettings.put("style", "collapsible");
        settings.sidebarSettings.put("defaultCollapsed", false);
        settings.sidebarSettings.put("showIcons", true);

        // 기본 푸터 설정
        settings.footerSettings = new HashMap<>();
        settings.footerSettings.put("enabled", true);
        settings.footerSettings.put("showLinks", true);
        settings.footerSettings.put("showCopyright", true);

        // 기본 콘텐츠 설정
        settings.contentSettings = new HashMap<>();
        settings.contentSettings.put("maxWidth", "full");
        settings.contentSettings.put("padding", "normal");

        return settings;
    }

    // 브랜딩 업데이트 (확장)
    public void updateBranding(String logoUrl, String darkLogoUrl, String faviconUrl,
                               String primaryColor, String secondaryColor, String accentColor,
                               String fontFamily, String headingFont, String bodyFont) {
        this.logoUrl = logoUrl;
        this.darkLogoUrl = darkLogoUrl;
        this.faviconUrl = faviconUrl;
        if (primaryColor != null) this.primaryColor = primaryColor;
        if (secondaryColor != null) this.secondaryColor = secondaryColor;
        if (accentColor != null) this.accentColor = accentColor;
        this.fontFamily = fontFamily;
        this.headingFont = headingFont;
        this.bodyFont = bodyFont;
    }

    // 레이아웃 설정 업데이트
    public void updateLayoutSettings(Map<String, Object> headerSettings,
                                     Map<String, Object> sidebarSettings,
                                     Map<String, Object> footerSettings,
                                     Map<String, Object> contentSettings) {
        if (headerSettings != null) this.headerSettings = headerSettings;
        if (sidebarSettings != null) this.sidebarSettings = sidebarSettings;
        if (footerSettings != null) this.footerSettings = footerSettings;
        if (contentSettings != null) this.contentSettings = contentSettings;
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

    // 테넌트 기능 On/Off 설정 업데이트
    public void updateTenantFeatures(Boolean communityEnabled, Boolean userCourseCreationEnabled,
                                     Boolean cartEnabled, Boolean wishlistEnabled,
                                     Boolean instructorTabEnabled, Boolean paidModeEnabled) {
        if (communityEnabled != null) this.communityEnabled = communityEnabled;
        if (userCourseCreationEnabled != null) this.userCourseCreationEnabled = userCourseCreationEnabled;
        if (cartEnabled != null) this.cartEnabled = cartEnabled;
        if (wishlistEnabled != null) this.wishlistEnabled = wishlistEnabled;
        if (instructorTabEnabled != null) this.instructorTabEnabled = instructorTabEnabled;
        if (paidModeEnabled != null) this.paidModeEnabled = paidModeEnabled;
    }

    // 확장 브랜딩 설정 업데이트
    public void updateExtendedBranding(String companyName,
                                       Map<String, Object> bannerSettings,
                                       Map<String, Object> landingPageSettings,
                                       Map<String, Object> sidebarTUSettings,
                                       Map<String, Object> sidebarCOSettings) {
        if (companyName != null) this.companyName = companyName;
        if (bannerSettings != null) this.bannerSettings = bannerSettings;
        if (landingPageSettings != null) this.landingPageSettings = landingPageSettings;
        if (sidebarTUSettings != null) this.sidebarTUSettings = sidebarTUSettings;
        if (sidebarCOSettings != null) this.sidebarCOSettings = sidebarCOSettings;
    }
}
