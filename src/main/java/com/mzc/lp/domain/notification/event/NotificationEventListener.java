package com.mzc.lp.domain.notification.event;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 이벤트 리스너
 * 비동기로 알림을 발송하여 메인 트랜잭션에 영향을 주지 않음
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationTemplateService templateService;

    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            // TenantContext 설정
            TenantContext.setTenantId(event.getTenantId());

            log.debug("Processing notification event: trigger={}, userId={}, tenantId={}",
                    event.getTrigger(), event.getUserId(), event.getTenantId());

            templateService.sendNotificationByTrigger(
                    event.getTrigger(),
                    event.getUserId(),
                    event.getVariables(),
                    event.getLink(),
                    event.getReferenceId(),
                    event.getReferenceType()
            );

            log.info("Notification sent: trigger={}, userId={}", event.getTrigger(), event.getUserId());
        } catch (Exception e) {
            // 알림 발송 실패는 메인 비즈니스 로직에 영향을 주지 않도록 로그만 남김
            log.error("Failed to send notification: trigger={}, userId={}, error={}",
                    event.getTrigger(), event.getUserId(), e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }
}
