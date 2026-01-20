-- =============================================
-- V006: 카테고리 데이터 (명시적 ID)
-- =============================================
-- ID 범위:
--   테넌트 1: 1-10
--   테넌트 2: 11-20
--   테넌트 3: 21-30

-- ===== 테넌트 1 (기본 테넌트) 카테고리 =====
INSERT INTO cm_categories (id, tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(1, 1, '개발', 'dev', 1, true, NOW(), NOW()),
(2, 1, 'AI', 'ai', 2, true, NOW(), NOW()),
(3, 1, '데이터', 'data', 3, true, NOW(), NOW()),
(4, 1, '디자인', 'design', 4, true, NOW(), NOW()),
(5, 1, '비즈니스', 'business', 5, true, NOW(), NOW()),
(6, 1, '마케팅', 'marketing', 6, true, NOW(), NOW()),
(7, 1, '외국어', 'language', 7, true, NOW(), NOW());

-- ===== 테넌트 2 (A사) 카테고리 =====
INSERT INTO cm_categories (id, tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(11, 2, '개발', 'dev', 1, true, NOW(), NOW()),
(12, 2, 'AI/ML', 'ai', 2, true, NOW(), NOW()),
(13, 2, '데이터', 'data', 3, true, NOW(), NOW()),
(14, 2, '클라우드', 'cloud', 4, true, NOW(), NOW()),
(15, 2, '보안', 'security', 5, true, NOW(), NOW());

-- ===== 테넌트 3 (B사) 카테고리 =====
INSERT INTO cm_categories (id, tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(21, 3, '프로그래밍', 'programming', 1, true, NOW(), NOW()),
(22, 3, '웹개발', 'web', 2, true, NOW(), NOW()),
(23, 3, '모바일', 'mobile', 3, true, NOW(), NOW()),
(24, 3, '데이터분석', 'analytics', 4, true, NOW(), NOW()),
(25, 3, '기획/PM', 'pm', 5, true, NOW(), NOW());
