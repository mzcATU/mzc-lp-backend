-- 기본 테넌트 초기 데이터
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 시스템 어드민 계정 (syadmin@admin.com / ehfkdl3dy!)
-- BCrypt hash for 'ehfkdl3dy!': $2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'syadmin@admin.com', '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', 'SystemAdmin', '010-0000-0001', 'SYSTEM_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'syadmin@admin.com' AND tenant_id = 1);

-- 테넌트 어드민 계정 (taadmin@admin.com / ehfkdl3dy!)
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'taadmin@admin.com', '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', 'TenantAdmin', '010-0000-0002', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'taadmin@admin.com' AND tenant_id = 1);

-- 기존 관리자 계정 비밀번호 업데이트 (이미 존재하는 경우)
UPDATE users SET password = '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', role = 'SYSTEM_ADMIN' WHERE email = 'syadmin@admin.com' AND tenant_id = 1;
UPDATE users SET password = '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', role = 'TENANT_ADMIN' WHERE email = 'taadmin@admin.com' AND tenant_id = 1;

-- 테스트 계정 역할 업데이트
UPDATE users SET role = 'DESIGNER' WHERE email = 'designer@test.com';
UPDATE users SET role = 'OPERATOR' WHERE email = 'operator@test.com';
