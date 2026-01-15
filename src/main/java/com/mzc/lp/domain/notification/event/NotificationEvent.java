package com.mzc.lp.domain.notification.event;

import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 알림 발송을 위한 이벤트
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final NotificationTrigger trigger;
    private final Long tenantId;
    private final Long userId;
    private final Map<String, String> variables;
    private final String link;
    private final Long referenceId;
    private final String referenceType;

    public NotificationEvent(
            Object source,
            NotificationTrigger trigger,
            Long tenantId,
            Long userId,
            Map<String, String> variables,
            String link,
            Long referenceId,
            String referenceType
    ) {
        super(source);
        this.trigger = trigger;
        this.tenantId = tenantId;
        this.userId = userId;
        this.variables = variables;
        this.link = link;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
    }

    public static NotificationEvent of(
            Object source,
            NotificationTrigger trigger,
            Long tenantId,
            Long userId,
            Map<String, String> variables
    ) {
        return new NotificationEvent(source, trigger, tenantId, userId, variables, null, null, null);
    }

    public static NotificationEvent of(
            Object source,
            NotificationTrigger trigger,
            Long tenantId,
            Long userId,
            Map<String, String> variables,
            String link,
            Long referenceId,
            String referenceType
    ) {
        return new NotificationEvent(source, trigger, tenantId, userId, variables, link, referenceId, referenceType);
    }
}
