package com.mzc.lp.domain.notification.dto.response;

import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import com.mzc.lp.domain.notification.entity.NotificationTemplate;

import java.time.Instant;

public record NotificationTemplateResponse(
        Long id,
        NotificationTrigger triggerType,
        String triggerDisplayName,
        NotificationCategory category,
        String categoryDisplayName,
        String name,
        String titleTemplate,
        String messageTemplate,
        String description,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getTriggerType(),
                template.getTriggerType().getDisplayName(),
                template.getCategory(),
                template.getCategory().getDisplayName(),
                template.getName(),
                template.getTitleTemplate(),
                template.getMessageTemplate(),
                template.getDescription(),
                template.getIsActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
