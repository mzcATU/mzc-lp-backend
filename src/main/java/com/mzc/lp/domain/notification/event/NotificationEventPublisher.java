package com.mzc.lp.domain.notification.event;

import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 알림 이벤트 퍼블리셔
 * 각 서비스에서 알림을 발송할 때 사용
 */
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 알림 이벤트 발행
     */
    public void publish(
            NotificationTrigger trigger,
            Long tenantId,
            Long userId,
            Map<String, String> variables
    ) {
        eventPublisher.publishEvent(
                NotificationEvent.of(this, trigger, tenantId, userId, variables)
        );
    }

    /**
     * 알림 이벤트 발행 (링크 및 참조 정보 포함)
     */
    public void publish(
            NotificationTrigger trigger,
            Long tenantId,
            Long userId,
            Map<String, String> variables,
            String link,
            Long referenceId,
            String referenceType
    ) {
        eventPublisher.publishEvent(
                NotificationEvent.of(this, trigger, tenantId, userId, variables, link, referenceId, referenceType)
        );
    }

    /**
     * 회원가입 환영 알림
     */
    public void publishWelcome(Long tenantId, Long userId, String userName) {
        publish(
                NotificationTrigger.WELCOME,
                tenantId,
                userId,
                Map.of("userName", userName)
        );
    }

    /**
     * 수강신청 완료 알림
     */
    public void publishEnrollmentComplete(Long tenantId, Long userId, String userName, String courseName, Long enrollmentId) {
        publish(
                NotificationTrigger.ENROLLMENT_COMPLETE,
                tenantId,
                userId,
                Map.of("userName", userName, "courseName", courseName),
                "/my-courses/" + enrollmentId,
                enrollmentId,
                "ENROLLMENT"
        );
    }

    /**
     * 과정 완료 축하 알림
     */
    public void publishCourseComplete(Long tenantId, Long userId, String userName, String courseName, Long enrollmentId) {
        publish(
                NotificationTrigger.COURSE_COMPLETE,
                tenantId,
                userId,
                Map.of("userName", userName, "courseName", courseName),
                "/my-courses/" + enrollmentId,
                enrollmentId,
                "ENROLLMENT"
        );
    }
}
