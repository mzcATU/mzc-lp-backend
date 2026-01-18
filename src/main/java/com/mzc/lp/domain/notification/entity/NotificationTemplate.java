package com.mzc.lp.domain.notification.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 템플릿 엔티티
 * 테넌트별로 알림 메시지 템플릿을 관리
 */
@Entity
@Table(
    name = "notification_templates",
    indexes = {
        @Index(name = "idx_notification_template_tenant", columnList = "tenant_id"),
        @Index(name = "idx_notification_template_trigger", columnList = "tenant_id, trigger_type"),
        @Index(name = "idx_notification_template_category", columnList = "tenant_id, category"),
        @Index(name = "idx_notification_template_active", columnList = "tenant_id, is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_template_trigger", columnNames = {"tenant_id", "trigger_type"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationTemplate extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private NotificationTrigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private NotificationCategory category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    private Long version;

    // ===== 정적 팩토리 메서드 =====
    public static NotificationTemplate create(
            NotificationTrigger triggerType,
            String name,
            String titleTemplate,
            String messageTemplate,
            String description
    ) {
        NotificationTemplate template = new NotificationTemplate();
        template.triggerType = triggerType;
        template.category = triggerType.getCategory();
        template.name = name;
        template.titleTemplate = titleTemplate;
        template.messageTemplate = messageTemplate;
        template.description = description;
        template.isActive = true;
        return template;
    }

    /**
     * 기본 템플릿 생성 (트리거 타입 기반)
     */
    public static NotificationTemplate createDefault(NotificationTrigger triggerType) {
        NotificationTemplate template = new NotificationTemplate();
        template.triggerType = triggerType;
        template.category = triggerType.getCategory();
        template.name = triggerType.getDisplayName();
        template.titleTemplate = getDefaultTitle(triggerType);
        template.messageTemplate = getDefaultMessage(triggerType);
        template.description = triggerType.getDisplayName() + " 시 발송";
        template.isActive = true;
        return template;
    }

    private static String getDefaultTitle(NotificationTrigger trigger) {
        return switch (trigger) {
            case WELCOME -> "회원가입을 환영합니다!";
            case ENROLLMENT_COMPLETE -> "수강신청이 완료되었습니다";
            case COURSE_COMPLETE -> "과정 수료를 축하드립니다!";
        };
    }

    private static String getDefaultMessage(NotificationTrigger trigger) {
        return switch (trigger) {
            case WELCOME -> "{{userName}}님, 가입을 환영합니다! 다양한 강좌를 둘러보세요.";
            case ENROLLMENT_COMPLETE -> "{{userName}}님, {{courseName}} 강좌 수강신청이 완료되었습니다.";
            case COURSE_COMPLETE -> "{{userName}}님, {{courseName}} 과정을 수료하신 것을 축하드립니다! 수료증을 확인해보세요.";
        };
    }

    // ===== 비즈니스 메서드 =====
    public void update(String name, String titleTemplate, String messageTemplate, String description) {
        this.name = name;
        this.titleTemplate = titleTemplate;
        this.messageTemplate = messageTemplate;
        this.description = description;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 템플릿 변수를 치환하여 실제 제목 생성
     */
    public String renderTitle(java.util.Map<String, String> variables) {
        return renderTemplate(this.titleTemplate, variables);
    }

    /**
     * 템플릿 변수를 치환하여 실제 메시지 생성
     */
    public String renderMessage(java.util.Map<String, String> variables) {
        return renderTemplate(this.messageTemplate, variables);
    }

    private String renderTemplate(String template, java.util.Map<String, String> variables) {
        String result = template;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
