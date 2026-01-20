-- =============================================
-- V012: 강사 배정 데이터
-- =============================================
-- 각 차수별 강사 배정 (코스 설계자 자동 배정 시나리오)
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 강사 배정 =====
INSERT INTO iis_instructor_assignments (id, tenant_id, user_key, time_key, assigned_at, role, status, assigned_by, created_at, updated_at, version) VALUES
-- Spring Boot 기초 차수들 (설계자: creator=8)
(1, 1, 8, 1, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
(2, 1, 8, 3, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
(3, 1, 8, 6, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
(4, 1, 8, 9, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
(5, 1, 8, 11, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
-- React & TypeScript 실전 1차 (설계자: creator=8)
(6, 1, 8, 4, NOW(), 'MAIN', 'ACTIVE', 8, NOW(), NOW(), 0),
-- AWS 클라우드 아키텍처 차수들 (설계자: designer1=5)
(7, 1, 5, 2, NOW(), 'MAIN', 'ACTIVE', 5, NOW(), NOW(), 0),
(8, 1, 5, 5, NOW(), 'MAIN', 'ACTIVE', 5, NOW(), NOW(), 0),
-- Java 프로그래밍 마스터 차수들 (설계자: designer1=5)
(9, 1, 5, 7, NOW(), 'MAIN', 'ACTIVE', 5, NOW(), NOW(), 0),
(10, 1, 5, 12, NOW(), 'MAIN', 'ACTIVE', 5, NOW(), NOW(), 0),
-- Kubernetes 운영 실무 1차 (설계자: designer2=6)
(11, 1, 6, 8, NOW(), 'MAIN', 'ACTIVE', 6, NOW(), NOW(), 0),
-- SQL 완전 정복 1차 (설계자: designer2=6)
(12, 1, 6, 10, NOW(), 'MAIN', 'ACTIVE', 6, NOW(), NOW(), 0),
-- 멀티롤 사용자 추가 강사 배정 (보조 강사)
(13, 1, 10, 6, NOW(), 'ASSISTANT', 'ACTIVE', 3, NOW(), NOW(), 0),
(14, 1, 10, 7, NOW(), 'ASSISTANT', 'ACTIVE', 3, NOW(), NOW(), 0);

-- ===== 테넌트 2 강사 배정 =====
INSERT INTO iis_instructor_assignments (id, tenant_id, user_key, time_key, assigned_at, role, status, assigned_by, created_at, updated_at, version) VALUES
(51, 2, 13, 51, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0),
(52, 2, 13, 52, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0),
(53, 2, 13, 53, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0),
(54, 2, 13, 54, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0),
(55, 2, 13, 55, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0),
(56, 2, 13, 56, NOW(), 'MAIN', 'ACTIVE', 13, NOW(), NOW(), 0);

-- ===== 테넌트 3 강사 배정 =====
INSERT INTO iis_instructor_assignments (id, tenant_id, user_key, time_key, assigned_at, role, status, assigned_by, created_at, updated_at, version) VALUES
(101, 3, 23, 101, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0),
(102, 3, 23, 102, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0),
(103, 3, 23, 103, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0),
(104, 3, 23, 104, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0),
(105, 3, 23, 105, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0),
(106, 3, 23, 106, NOW(), 'MAIN', 'ACTIVE', 23, NOW(), NOW(), 0);
