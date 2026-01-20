-- =============================================
-- V018: 회원 풀 및 자동 입과 규칙 데이터
-- =============================================
-- 회원 풀: 조건 기반 회원 그룹 정의
-- 자동 입과 규칙: 특정 트리거 시 자동 수강 등록

-- ===== 테넌트 1 회원 풀 (ID: 1-10) =====
INSERT INTO member_pools (id, tenant_id, name, description, department_ids, positions, job_titles, employee_statuses, is_active, sort_order, created_at, updated_at) VALUES
-- 부서 기반 풀
(1, 1, '개발팀 전체', '개발팀 소속 전 직원', '1', NULL, NULL, NULL, true, 1, NOW(), NOW()),
(2, 1, '마케팅팀 전체', '마케팅팀 소속 전 직원', '2', NULL, NULL, NULL, true, 2, NOW(), NOW()),
(3, 1, '인사팀 전체', '인사팀 소속 전 직원', '3', NULL, NULL, NULL, true, 3, NOW(), NOW()),
-- 직급 기반 풀
(4, 1, '신입사원', '사원/인턴 직급', NULL, '사원,인턴', NULL, NULL, true, 4, NOW(), NOW()),
(5, 1, '관리자급', '팀장 이상 직급', NULL, '팀장,차장,부장', NULL, NULL, true, 5, NOW(), NOW()),
-- 복합 조건 풀
(6, 1, '개발팀 신입', '개발팀 소속 사원/인턴', '1', '사원,인턴', NULL, NULL, true, 6, NOW(), NOW()),
(7, 1, '영업/마케팅 전체', '영업팀, 마케팅팀 소속 전 직원', '2,4', NULL, NULL, NULL, true, 7, NOW(), NOW());

-- ===== 테넌트 2 회원 풀 (ID: 11-20) =====
INSERT INTO member_pools (id, tenant_id, name, description, department_ids, positions, job_titles, employee_statuses, is_active, sort_order, created_at, updated_at) VALUES
(11, 2, '개발팀 전체', '개발팀 소속 전 직원', '11', NULL, NULL, NULL, true, 1, NOW(), NOW()),
(12, 2, '기획팀 전체', '기획팀 소속 전 직원', '12', NULL, NULL, NULL, true, 2, NOW(), NOW()),
(13, 2, '품질관리팀 전체', '품질관리팀 소속 전 직원', '15', NULL, NULL, NULL, true, 3, NOW(), NOW()),
(14, 2, '신입사원', '사원/인턴 직급', NULL, '사원,인턴', NULL, NULL, true, 4, NOW(), NOW()),
(15, 2, '기술직군', '개발팀, 품질관리팀 전체', '11,15', NULL, NULL, NULL, true, 5, NOW(), NOW());

-- ===== 테넌트 3 회원 풀 (ID: 21-30) =====
INSERT INTO member_pools (id, tenant_id, name, description, department_ids, positions, job_titles, employee_statuses, is_active, sort_order, created_at, updated_at) VALUES
(21, 3, '개발팀 전체', '개발팀 소속 전 직원', '21', NULL, NULL, NULL, true, 1, NOW(), NOW()),
(22, 3, '마케팅팀 전체', '마케팅팀 소속 전 직원', '22', NULL, NULL, NULL, true, 2, NOW(), NOW()),
(23, 3, '운영팀 전체', '운영팀 소속 전 직원', '23', NULL, NULL, NULL, true, 3, NOW(), NOW()),
(24, 3, '고객지원팀 전체', '고객지원팀 소속 전 직원', '24', NULL, NULL, NULL, true, 4, NOW(), NOW()),
(25, 3, '신입사원', '사원/인턴 직급', NULL, '사원,인턴', NULL, NULL, true, 5, NOW(), NOW());


-- ===== 테넌트 1 자동 입과 규칙 (ID: 1-10) =====
INSERT INTO auto_enrollment_rules (id, tenant_id, name, description, trigger_type, department_id, course_time_id, is_active, sort_order, created_at, updated_at) VALUES
-- 신규 입사자 교육
(1, 1, '신규 입사자 기본 교육', '신규 가입 시 Spring Boot 기초 과정 자동 등록', 'USER_JOIN', NULL, 3, true, 1, NOW(), NOW()),
-- 부서 배정 시 교육
(2, 1, '개발팀 배정 시 Java 교육', '개발팀 배정 시 Java 마스터 과정 자동 등록', 'DEPARTMENT_ASSIGN', 1, 7, true, 2, NOW(), NOW()),
(3, 1, '개발팀 배정 시 SQL 교육', '개발팀 배정 시 SQL 완전 정복 자동 등록', 'DEPARTMENT_ASSIGN', 1, 15, true, 3, NOW(), NOW()),
-- 비활성화된 규칙 (테스트용)
(4, 1, '마케팅팀 교육 (비활성)', '마케팅팀 배정 시 자동 등록 (현재 비활성)', 'DEPARTMENT_ASSIGN', 2, 4, false, 4, NOW(), NOW());

-- ===== 테넌트 2 자동 입과 규칙 (ID: 11-20) =====
INSERT INTO auto_enrollment_rules (id, tenant_id, name, description, trigger_type, department_id, course_time_id, is_active, sort_order, created_at, updated_at) VALUES
(11, 2, '신규 입사자 Java 교육', '신규 가입 시 Java 기초 과정 자동 등록', 'USER_JOIN', NULL, 51, true, 1, NOW(), NOW()),
(12, 2, '개발팀 배정 시 Spring 교육', '개발팀 배정 시 Spring Framework 과정 자동 등록', 'DEPARTMENT_ASSIGN', 11, 52, true, 2, NOW(), NOW()),
(13, 2, '품질관리팀 클라우드 교육', '품질관리팀 배정 시 클라우드 네이티브 과정 자동 등록', 'DEPARTMENT_ASSIGN', 15, 53, true, 3, NOW(), NOW());

-- ===== 테넌트 3 자동 입과 규칙 (ID: 21-30) =====
INSERT INTO auto_enrollment_rules (id, tenant_id, name, description, trigger_type, department_id, course_time_id, is_active, sort_order, created_at, updated_at) VALUES
(21, 3, '신규 입사자 React Native 교육', '신규 가입 시 React Native 과정 자동 등록', 'USER_JOIN', NULL, 101, true, 1, NOW(), NOW()),
(22, 3, '개발팀 배정 시 풀스택 교육', '개발팀 배정 시 웹 개발 풀스택 과정 자동 등록', 'DEPARTMENT_ASSIGN', 21, 103, true, 2, NOW(), NOW()),
(23, 3, '마케팅팀 데이터 시각화 교육', '마케팅팀 배정 시 데이터 시각화 과정 자동 등록', 'DEPARTMENT_ASSIGN', 22, 104, true, 3, NOW(), NOW());
