-- =============================================
-- V003: 부서 데이터 (테넌트별, 명시적 ID)
-- =============================================
-- ID 범위:
--   테넌트 1: 1-10
--   테넌트 2: 11-20
--   테넌트 3: 21-30

-- ===== 테넌트 1 (기본 테넌트) 부서들 =====
INSERT INTO departments (id, tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(1, 1, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(2, 1, '마케팅팀', 'MKT', '마케팅 및 홍보 부서', NULL, 2, true, NOW(), NOW()),
(3, 1, '인사팀', 'HR', '인사 관리 부서', NULL, 3, true, NOW(), NOW()),
(4, 1, '영업팀', 'SALES', '영업 및 고객 관리 부서', NULL, 4, true, NOW(), NOW()),
(5, 1, '디자인팀', 'DESIGN', 'UI/UX 및 그래픽 디자인 부서', NULL, 5, true, NOW(), NOW()),
(6, 1, '경영지원팀', 'BIZ', '경영 지원 부서', NULL, 6, true, NOW(), NOW());

-- ===== 테넌트 2 (A사) 부서들 =====
INSERT INTO departments (id, tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(11, 2, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(12, 2, '기획팀', 'PLAN', '서비스 기획 부서', NULL, 2, true, NOW(), NOW()),
(13, 2, '인사팀', 'HR', '인사 관리 부서', NULL, 3, true, NOW(), NOW()),
(14, 2, '재무팀', 'FIN', '재무 관리 부서', NULL, 4, true, NOW(), NOW()),
(15, 2, '품질관리팀', 'QA', '품질 보증 부서', NULL, 5, true, NOW(), NOW());

-- ===== 테넌트 3 (B사) 부서들 =====
INSERT INTO departments (id, tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(21, 3, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(22, 3, '마케팅팀', 'MKT', '마케팅 부서', NULL, 2, true, NOW(), NOW()),
(23, 3, '운영팀', 'OPS', '서비스 운영 부서', NULL, 3, true, NOW(), NOW()),
(24, 3, '고객지원팀', 'CS', '고객 지원 부서', NULL, 4, true, NOW(), NOW());
