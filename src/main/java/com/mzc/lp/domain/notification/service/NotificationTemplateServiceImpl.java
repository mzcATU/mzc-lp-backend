package com.mzc.lp.domain.notification.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.dto.request.CreateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.request.UpdateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.response.NotificationTemplateResponse;
import com.mzc.lp.domain.notification.entity.NotificationTemplate;
import com.mzc.lp.domain.notification.exception.DuplicateNotificationTemplateException;
import com.mzc.lp.domain.notification.exception.NotificationTemplateNotFoundException;
import com.mzc.lp.domain.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 중복 체크
        if (templateRepository.existsByTenantIdAndTriggerType(tenantId, request.triggerType())) {
            throw new DuplicateNotificationTemplateException(request.triggerType());
        }

        NotificationTemplate template = NotificationTemplate.create(
                request.triggerType(),
                request.name(),
                request.titleTemplate(),
                request.messageTemplate(),
                request.description()
        );

        templateRepository.save(template);
        log.info("Created notification template: {} for tenant: {}", request.triggerType(), tenantId);

        return NotificationTemplateResponse.from(template);
    }

    @Override
    @Transactional
    public void initializeDefaultTemplates() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Initializing default notification templates for tenant: {}", tenantId);

        for (NotificationTrigger trigger : NotificationTrigger.values()) {
            if (!templateRepository.existsByTenantIdAndTriggerType(tenantId, trigger)) {
                NotificationTemplate template = NotificationTemplate.createDefault(trigger);
                templateRepository.save(template);
                log.debug("Created default template: {} for tenant: {}", trigger, tenantId);
            }
        }
    }

    @Override
    @Transactional
    public List<NotificationTemplateResponse> getTemplates(NotificationCategory category) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 누락된 기본 템플릿이 있으면 자동 생성
        long templateCount = templateRepository.countByTenantId(tenantId);
        int triggerCount = NotificationTrigger.values().length;

        if (templateCount < triggerCount) {
            log.info("Missing templates found for tenant: {}. Current: {}, Expected: {}. Creating missing templates.",
                    tenantId, templateCount, triggerCount);

            // 누락된 템플릿 생성
            for (NotificationTrigger trigger : NotificationTrigger.values()) {
                if (!templateRepository.existsByTenantIdAndTriggerType(tenantId, trigger)) {
                    NotificationTemplate template = NotificationTemplate.createDefault(trigger);
                    templateRepository.save(template);
                    log.info("Created default template: {} for tenant: {}", trigger, tenantId);
                }
            }
        }

        List<NotificationTemplate> templates;
        if (category != null) {
            templates = templateRepository.findByTenantIdAndCategoryOrderByCreatedAtDesc(tenantId, category);
        } else {
            templates = templateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        }

        return templates.stream()
                .map(NotificationTemplateResponse::from)
                .toList();
    }

    @Override
    public NotificationTemplateResponse getTemplate(Long id) {
        NotificationTemplate template = findTemplate(id);
        return NotificationTemplateResponse.from(template);
    }

    @Override
    public NotificationTemplateResponse getTemplateByTrigger(NotificationTrigger triggerType) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NotificationTemplate template = templateRepository.findByTenantIdAndTriggerType(tenantId, triggerType)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(0L));
        return NotificationTemplateResponse.from(template);
    }

    @Override
    @Transactional
    public NotificationTemplateResponse updateTemplate(Long id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate template = findTemplate(id);
        template.update(
                request.name(),
                request.titleTemplate(),
                request.messageTemplate(),
                request.description()
        );

        log.info("Updated notification template: {}", id);
        return NotificationTemplateResponse.from(template);
    }

    @Override
    @Transactional
    public void activateTemplate(Long id) {
        NotificationTemplate template = findTemplate(id);
        template.activate();
        log.info("Activated notification template: {}", id);
    }

    @Override
    @Transactional
    public void deactivateTemplate(Long id) {
        NotificationTemplate template = findTemplate(id);
        template.deactivate();
        log.info("Deactivated notification template: {}", id);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        NotificationTemplate template = findTemplate(id);
        templateRepository.delete(template);
        log.info("Deleted notification template: {}", id);
    }

    @Override
    @Transactional
    public NotificationTemplateResponse duplicateTemplate(Long id) {
        NotificationTemplate original = findTemplate(id);

        // 복제된 템플릿은 다른 트리거 타입을 가질 수 없으므로, 이름만 변경하여 저장
        // 실제로는 같은 트리거 타입으로 복제 불가
        throw new UnsupportedOperationException("트리거 타입이 동일한 템플릿은 복제할 수 없습니다. 다른 트리거 타입으로 새 템플릿을 생성해주세요.");
    }

    @Override
    @Transactional
    public void sendNotificationByTrigger(
            NotificationTrigger trigger,
            Long userId,
            Map<String, String> variables,
            String link,
            Long referenceId,
            String referenceType
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 활성화된 템플릿 조회
        Optional<NotificationTemplate> templateOpt = templateRepository
                .findByTenantIdAndTriggerTypeAndIsActiveTrue(tenantId, trigger);

        // 템플릿이 없으면 기본 템플릿 자동 생성
        if (templateOpt.isEmpty()) {
            // 해당 트리거의 템플릿이 아예 없는지 확인
            if (!templateRepository.existsByTenantIdAndTriggerType(tenantId, trigger)) {
                log.info("No template found for trigger: {} in tenant: {}. Creating default template.", trigger, tenantId);
                NotificationTemplate defaultTemplate = NotificationTemplate.createDefault(trigger);
                templateRepository.save(defaultTemplate);
                templateOpt = Optional.of(defaultTemplate);
                log.info("Created default template for trigger: {} in tenant: {}", trigger, tenantId);
            } else {
                // 템플릿은 있지만 비활성화 상태
                log.debug("Template exists but is deactivated for trigger: {} in tenant: {}", trigger, tenantId);
                return;
            }
        }

        NotificationTemplate template = templateOpt.get();

        // 템플릿 변수 치환
        String title = template.renderTitle(variables);
        String message = template.renderMessage(variables);

        // 알림 타입 매핑
        NotificationType notificationType = mapTriggerToNotificationType(trigger);

        // 알림 생성
        notificationService.createNotification(
                userId,
                notificationType,
                title,
                message,
                link,
                referenceId,
                referenceType,
                null,  // actorId
                null   // actorName
        );

        log.info("Sent notification for trigger: {} to user: {}", trigger, userId);
    }

    private NotificationTemplate findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(id));
    }

    private NotificationType mapTriggerToNotificationType(NotificationTrigger trigger) {
        return switch (trigger) {
            case WELCOME -> NotificationType.SYSTEM;
            case ENROLLMENT_COMPLETE, COURSE_COMPLETE -> NotificationType.COURSE;
        };
    }
}
