-- =============================================
-- V002: 테넌트 데이터 (3개 테넌트)
-- =============================================

-- 테넌트 1: 기본 테넌트
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 테넌트 2: 삼성전자 (대기업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 2, 'samsung', '삼성전자 인재개발원', 'B2B', 'ACTIVE', 'PRO', 'samsung', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 2);

-- 테넌트 3: 네이버 (IT 기업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 3, 'naver', '네이버 커넥트', 'B2B', 'ACTIVE', 'BASIC', 'naver', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 3);
