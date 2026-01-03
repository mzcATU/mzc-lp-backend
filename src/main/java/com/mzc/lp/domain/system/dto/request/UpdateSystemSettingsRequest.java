package com.mzc.lp.domain.system.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 시스템 설정 업데이트 요청 DTO
 */
public record UpdateSystemSettingsRequest(
        // 일반 설정
        @Size(max = 200) String platformName,
        @Size(max = 50) String timezone,
        @Size(max = 10) String language,
        Boolean maintenanceMode,
        @Size(max = 500) String maintenanceMessage,

        // 보안 설정
        @Min(1) @Max(1440) Integer sessionTimeout,
        @Min(1) @Max(100) Integer maxLoginAttempts,
        @Min(0) @Max(365) Integer passwordExpiry,
        Boolean mfaRequired,
        @Size(max = 1000) String ipWhitelist,
        @Min(1) @Max(1440) Integer loginLockoutMinutes,

        // 스토리지 설정
        @Min(1) @Max(10000) Integer maxUploadSize,
        @Size(max = 500) String allowedFormats,
        Boolean autoCleanup,
        @Min(1) @Max(365) Integer cleanupDays,
        @Min(1) Long totalStorageLimitGB,

        // 이메일 설정
        @Size(max = 200) String smtpHost,
        @Min(1) @Max(65535) Integer smtpPort,
        @Size(max = 200) String smtpUser,
        @Size(max = 500) String smtpPassword,
        Boolean useTls,
        @Size(max = 200) String senderEmail,
        @Size(max = 100) String senderName
) {}
