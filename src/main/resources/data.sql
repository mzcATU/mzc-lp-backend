-- 기본 테넌트 초기 데이터
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 시스템 어드민 계정 (syadmin@admin.com / ehfkdl3dy!)
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'syadmin@admin.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQbLgIMdjnQJHG8.l1pY6MCLah5K2G', '시스템관리자', '010-0000-0001', 'SYSTEM_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'syadmin@admin.com' AND tenant_id = 1);

-- 테넌트 어드민 계정 (taadmin@admin.com / ehfkdl3dy!)
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'taadmin@admin.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQbLgIMdjnQJHG8.l1pY6MCLah5K2G', '테넌트관리자', '010-0000-0002', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'taadmin@admin.com' AND tenant_id = 1);

-- 테스트 계정 역할 업데이트
UPDATE users SET role = 'DESIGNER' WHERE email = 'designer@test.com';
UPDATE users SET role = 'OPERATOR' WHERE email = 'operator@test.com';
