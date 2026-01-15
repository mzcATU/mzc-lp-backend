-- 알림 템플릿 테이블 생성
CREATE TABLE notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    message_template TEXT NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_notification_template_trigger UNIQUE (tenant_id, trigger_type),
    CONSTRAINT fk_notification_template_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- 인덱스 추가
CREATE INDEX idx_notification_template_tenant ON notification_templates(tenant_id);
CREATE INDEX idx_notification_template_trigger ON notification_templates(tenant_id, trigger_type);
CREATE INDEX idx_notification_template_category ON notification_templates(tenant_id, category);
CREATE INDEX idx_notification_template_active ON notification_templates(tenant_id, is_active);
