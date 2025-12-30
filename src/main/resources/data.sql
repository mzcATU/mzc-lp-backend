-- 기본 테넌트 초기 데이터
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 테스트 계정 역할 업데이트
UPDATE users SET role = 'DESIGNER' WHERE email = 'designer@test.com';
UPDATE users SET role = 'OPERATOR' WHERE email = 'operator@test.com';
