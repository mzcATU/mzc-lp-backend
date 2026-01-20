-- =============================================
-- V013: 사용자 코스 역할 데이터
-- =============================================
-- USER → DESIGNER 흐름에서의 역할 부여
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 사용자 코스 역할 =====
INSERT INTO user_course_roles (id, tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at) VALUES
-- creator@default.com: USER + DESIGNER (프로그램 승인받음)
-- DESIGNER 역할 (courseId = null, 테넌트 레벨)
(1, 1, 8, NULL, 'DESIGNER', NULL, NOW(), NOW()),
-- DESIGNER 역할 (courseId = programId, 프로그램별 설계자)
(2, 1, 8, 10, 'DESIGNER', 70, NOW(), NOW()),
(3, 1, 8, 12, 'DESIGNER', 70, NOW(), NOW()),

-- designer1@default.com: DESIGNER 역할
(4, 1, 5, NULL, 'DESIGNER', NULL, NOW(), NOW()),
(5, 1, 5, 11, 'DESIGNER', 70, NOW(), NOW()),
(6, 1, 5, 13, 'DESIGNER', 70, NOW(), NOW()),

-- designer2@default.com: DESIGNER 역할
(7, 1, 6, NULL, 'DESIGNER', NULL, NOW(), NOW()),
(8, 1, 6, 14, 'DESIGNER', 65, NOW(), NOW()),
(9, 1, 6, 15, 'DESIGNER', 65, NOW(), NOW()),

-- designer3@default.com: USER + DESIGNER (프로그램 아직 미승인)
-- DESIGNER 역할만 (courseId = null, 테넌트 레벨)
(10, 1, 7, NULL, 'DESIGNER', NULL, NOW(), NOW()),

-- 멀티롤 사용자 역할 (DESIGNER + INSTRUCTOR)
(11, 1, 10, NULL, 'DESIGNER', NULL, NOW(), NOW()),
(12, 1, 10, 10, 'INSTRUCTOR', 30, NOW(), NOW());

-- ===== 테넌트 2 사용자 코스 역할 =====
INSERT INTO user_course_roles (id, tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at) VALUES
(51, 2, 13, NULL, 'DESIGNER', NULL, NOW(), NOW()),
(52, 2, 13, 51, 'DESIGNER', 70, NOW(), NOW()),
(53, 2, 13, 52, 'DESIGNER', 70, NOW(), NOW()),
(54, 2, 13, 53, 'DESIGNER', 65, NOW(), NOW()),
(55, 2, 14, NULL, 'DESIGNER', NULL, NOW(), NOW());

-- ===== 테넌트 3 사용자 코스 역할 =====
INSERT INTO user_course_roles (id, tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at) VALUES
(101, 3, 23, NULL, 'DESIGNER', NULL, NOW(), NOW()),
(102, 3, 23, 101, 'DESIGNER', 70, NOW(), NOW()),
(103, 3, 23, 102, 'DESIGNER', 70, NOW(), NOW()),
(104, 3, 23, 103, 'DESIGNER', 65, NOW(), NOW()),
(105, 3, 24, NULL, 'DESIGNER', NULL, NOW(), NOW());
