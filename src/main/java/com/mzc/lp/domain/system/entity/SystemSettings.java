package com.mzc.lp.domain.system.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시스템 전역 설정 엔티티 (SA 관리)
 * 플랫폼 전체에 적용되는 설정을 관리
 * 단일 레코드만 존재 (Singleton)
 */
@Entity
@Table(name = "system_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemSettings extends BaseTimeEntity {

    @Column(nullable = false, unique = true)
    private String settingsKey = "GLOBAL";

    // ============================================
    // 일반 설정
    // ============================================

    @Column(length = 200, nullable = false)
    private String platformName = "MZC Learn Platform";

    @Column(length = 50, nullable = false)
    private String timezone = "Asia/Seoul";

    @Column(length = 10, nullable = false)
    private String language = "ko";

    @Column(nullable = false)
    private Boolean maintenanceMode = false;

    @Column(length = 500)
    private String maintenanceMessage;

    // ============================================
    // 보안 설정
    // ============================================

    @Column(nullable = false)
    private Integer sessionTimeoutMinutes = 30;

    @Column(nullable = false)
    private Integer maxLoginAttempts = 5;

    @Column(nullable = false)
    private Integer passwordExpiryDays = 90;

    @Column(nullable = false)
    private Boolean mfaRequired = false;

    @Column(length = 1000)
    private String ipWhitelist;

    @Column(nullable = false)
    private Integer loginLockoutMinutes = 15;

    // ============================================
    // 스토리지 설정
    // ============================================

    @Column(nullable = false)
    private Integer maxUploadSizeMB = 500;

    @Column(length = 500, nullable = false)
    private String allowedFileFormats = "mp4,webm,pdf,pptx,docx,xlsx,jpg,png,gif";

    @Column(nullable = false)
    private Boolean autoCleanupEnabled = true;

    @Column(nullable = false)
    private Integer cleanupDays = 30;

    @Column(nullable = false)
    private Long totalStorageLimitGB = 1000L;

    // ============================================
    // 이메일 설정
    // ============================================

    @Column(length = 200)
    private String smtpHost;

    @Column
    private Integer smtpPort = 587;

    @Column(length = 200)
    private String smtpUser;

    @Column(length = 500)
    private String smtpPassword;

    @Column(nullable = false)
    private Boolean smtpUseTls = true;

    @Column(length = 200)
    private String senderEmail;

    @Column(length = 100)
    private String senderName;

    // 정적 팩토리 메서드
    public static SystemSettings createDefault() {
        SystemSettings settings = new SystemSettings();
        settings.settingsKey = "GLOBAL";
        return settings;
    }

    // 일반 설정 업데이트
    public void updateGeneralSettings(String platformName, String timezone,
                                      String language, Boolean maintenanceMode,
                                      String maintenanceMessage) {
        if (platformName != null) this.platformName = platformName;
        if (timezone != null) this.timezone = timezone;
        if (language != null) this.language = language;
        if (maintenanceMode != null) this.maintenanceMode = maintenanceMode;
        this.maintenanceMessage = maintenanceMessage;
    }

    // 보안 설정 업데이트
    public void updateSecuritySettings(Integer sessionTimeoutMinutes, Integer maxLoginAttempts,
                                       Integer passwordExpiryDays, Boolean mfaRequired,
                                       String ipWhitelist, Integer loginLockoutMinutes) {
        if (sessionTimeoutMinutes != null) this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        if (maxLoginAttempts != null) this.maxLoginAttempts = maxLoginAttempts;
        if (passwordExpiryDays != null) this.passwordExpiryDays = passwordExpiryDays;
        if (mfaRequired != null) this.mfaRequired = mfaRequired;
        this.ipWhitelist = ipWhitelist;
        if (loginLockoutMinutes != null) this.loginLockoutMinutes = loginLockoutMinutes;
    }

    // 스토리지 설정 업데이트
    public void updateStorageSettings(Integer maxUploadSizeMB, String allowedFileFormats,
                                      Boolean autoCleanupEnabled, Integer cleanupDays,
                                      Long totalStorageLimitGB) {
        if (maxUploadSizeMB != null) this.maxUploadSizeMB = maxUploadSizeMB;
        if (allowedFileFormats != null) this.allowedFileFormats = allowedFileFormats;
        if (autoCleanupEnabled != null) this.autoCleanupEnabled = autoCleanupEnabled;
        if (cleanupDays != null) this.cleanupDays = cleanupDays;
        if (totalStorageLimitGB != null) this.totalStorageLimitGB = totalStorageLimitGB;
    }

    // 이메일 설정 업데이트
    public void updateEmailSettings(String smtpHost, Integer smtpPort, String smtpUser,
                                    String smtpPassword, Boolean smtpUseTls,
                                    String senderEmail, String senderName) {
        this.smtpHost = smtpHost;
        if (smtpPort != null) this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        if (smtpUseTls != null) this.smtpUseTls = smtpUseTls;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }
}
