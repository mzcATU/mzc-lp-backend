package com.mzc.lp.domain.analytics.dto.response;

import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.entity.ActivityLog;

import java.time.Instant;

/**
 * 활동 로그 응답 DTO
 */
public record ActivityLogResponse(
        Long id,
        Long tenantId,
        Long userId,
        String userName,
        String userEmail,
        ActivityType activityType,
        String activityTypeLabel,
        String description,
        String targetType,
        Long targetId,
        String targetName,
        String ipAddress,
        Instant createdAt
) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getTenantId(),
                log.getUserId(),
                log.getUserName(),
                log.getUserEmail(),
                log.getActivityType(),
                getActivityTypeLabel(log.getActivityType()),
                log.getDescription(),
                log.getTargetType(),
                log.getTargetId(),
                log.getTargetName(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }

    private static String getActivityTypeLabel(ActivityType type) {
        return switch (type) {
            case LOGIN -> "로그인";
            case LOGOUT -> "로그아웃";
            case LOGIN_FAILED -> "로그인 실패";
            case PASSWORD_CHANGE -> "비밀번호 변경";
            case USER_CREATE -> "사용자 생성";
            case USER_UPDATE -> "사용자 수정";
            case USER_DELETE -> "사용자 삭제";
            case ROLE_CHANGE -> "역할 변경";
            case COURSE_VIEW -> "강좌 조회";
            case COURSE_CREATE -> "강좌 생성";
            case COURSE_UPDATE -> "강좌 수정";
            case COURSE_DELETE -> "강좌 삭제";
            case PROGRAM_CREATE -> "프로그램 생성";
            case PROGRAM_UPDATE -> "프로그램 수정";
            case PROGRAM_APPROVE -> "프로그램 승인";
            case PROGRAM_REJECT -> "프로그램 반려";
            case ENROLLMENT_CREATE -> "수강 신청";
            case ENROLLMENT_COMPLETE -> "수강 완료";
            case ENROLLMENT_DROP -> "수강 취소";
            case CONTENT_VIEW -> "콘텐츠 조회";
            case CONTENT_COMPLETE -> "콘텐츠 완료";
            case SETTINGS_UPDATE -> "설정 변경";
            case TENANT_CREATE -> "테넌트 생성";
            case TENANT_UPDATE -> "테넌트 수정";
            case OTHER -> "기타";
        };
    }
}
