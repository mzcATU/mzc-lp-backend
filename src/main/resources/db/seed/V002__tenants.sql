-- =============================================
-- V002: 테넌트 데이터 (3개 테넌트)
-- =============================================

-- 테넌트 1: 기본 테넌트
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 테넌트 2: A사 (기업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 2, 'company-a', 'A사 교육센터', 'B2B', 'ACTIVE', 'PRO', 'company-a', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 2);

-- 테넌트 3: B사 (스타트업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 3, 'company-b', 'B사 아카데미', 'B2B', 'ACTIVE', 'BASIC', 'company-b', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 3);
