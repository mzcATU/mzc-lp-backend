-- =============================================
-- V010: 차수 데이터 (명시적 ID)
-- =============================================
-- 다양한 상태의 차수 데이터 포함
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 차수 (차수 ID = 스냅샷 ID로 1:1 매핑) =====
INSERT INTO course_times (id, tenant_id, course_id, snapshot_id, title, delivery_type, duration_type, duration_days, status, enroll_start_date, enroll_end_date, class_start_date, class_end_date, capacity, max_waiting_count, current_enrollment, enrollment_method, min_progress_for_completion, price, is_free, allow_late_enrollment, created_by, created_at, updated_at, version) VALUES
-- 1. DRAFT (작성 중) - 모집 시작일이 미래
(1, 1, 10, 1, 'Spring Boot 기초 1차', 'ONLINE', 'FIXED', 29, 'DRAFT', '2026-02-01', '2026-02-15', '2026-02-20', '2026-03-20', 30, 5, 0, 'FIRST_COME', 80, 0.00, true, false, 3, NOW(), NOW(), 0),
(2, 1, 11, 2, 'AWS 클라우드 아키텍처 1차', 'BLENDED', 'FIXED', 62, 'DRAFT', '2026-03-01', '2026-03-15', '2026-03-20', '2026-05-20', 20, 3, 0, 'APPROVAL', 70, 150000.00, false, false, 3, NOW(), NOW(), 0),
-- 2. RECRUITING (모집 중) - 현재가 모집 기간 내
(3, 1, 10, 3, 'Spring Boot 기초 2차', 'ONLINE', 'FIXED', 32, 'RECRUITING', '2026-01-01', '2026-01-25', '2026-01-28', '2026-02-28', 25, 5, 12, 'FIRST_COME', 80, 0.00, true, true, 3, NOW(), NOW(), 0),
(4, 1, 12, 4, 'React & TypeScript 실전 1차', 'LIVE', 'FIXED', 32, 'RECRUITING', '2025-12-20', '2026-01-25', '2026-01-28', '2026-02-28', 40, 10, 28, 'FIRST_COME', 75, 200000.00, false, true, 3, NOW(), NOW(), 0),
(5, 1, 11, 5, 'AWS 클라우드 아키텍처 2차', 'OFFLINE', 'FIXED', 29, 'RECRUITING', '2026-01-02', '2026-01-25', '2026-02-01', '2026-03-01', 15, 0, 15, 'APPROVAL', 70, 180000.00, false, false, 4, NOW(), NOW(), 0),
-- 3. ONGOING (진행 중) - 현재가 교육 기간 내
(6, 1, 10, 6, 'Spring Boot 기초 3차', 'ONLINE', 'FIXED', 62, 'ONGOING', '2025-11-01', '2025-11-30', '2025-12-01', '2026-01-31', 30, 5, 30, 'FIRST_COME', 80, 0.00, true, true, 3, NOW(), NOW(), 0),
(7, 1, 13, 7, 'Java 프로그래밍 마스터 1차', 'ONLINE', 'FIXED', 63, 'ONGOING', '2025-11-15', '2025-12-15', '2025-12-20', '2026-02-20', 50, 10, 45, 'FIRST_COME', 85, 100000.00, false, false, 3, NOW(), NOW(), 0),
(8, 1, 14, 8, 'Kubernetes 운영 실무 1차', 'BLENDED', 'FIXED', 86, 'ONGOING', '2025-10-01', '2025-10-31', '2025-11-01', '2026-01-25', 20, 5, 18, 'APPROVAL', 90, 300000.00, false, true, 4, NOW(), NOW(), 0),
-- 4. CLOSED (종료) - 교육 기간이 과거
(9, 1, 10, 9, 'Spring Boot 기초 4차', 'ONLINE', 'FIXED', 31, 'CLOSED', '2025-09-01', '2025-09-15', '2025-09-20', '2025-10-20', 30, 5, 28, 'FIRST_COME', 80, 0.00, true, false, 3, NOW(), NOW(), 0),
(10, 1, 15, 10, 'SQL 완전 정복 1차', 'ONLINE', 'FIXED', 42, 'CLOSED', '2025-11-01', '2025-11-15', '2025-11-20', '2025-12-31', 100, 20, 87, 'FIRST_COME', 70, 50000.00, false, true, 4, NOW(), NOW(), 0),
-- 5. ARCHIVED (보관) - 먼 과거
(11, 1, 10, 11, 'Spring Boot 기초 0차 (파일럿)', 'ONLINE', 'FIXED', 32, 'ARCHIVED', '2025-01-01', '2025-01-15', '2025-01-20', '2025-02-20', 10, 0, 10, 'INVITE_ONLY', 80, 0.00, true, false, 3, NOW(), NOW(), 0),
(12, 1, 13, 12, 'Java 프로그래밍 마스터 0차', 'OFFLINE', 'FIXED', 62, 'ARCHIVED', '2025-03-01', '2025-03-15', '2025-03-20', '2025-05-20', 20, 5, 18, 'APPROVAL', 85, 80000.00, false, false, 4, NOW(), NOW(), 0),
-- 6. 테스트용 추가 차수
-- INVITE_ONLY + RECRUITING (선발제 모집중)
(13, 1, 14, 13, 'Kubernetes 운영 실무 특별반', 'BLENDED', 'FIXED', 45, 'RECRUITING', '2026-01-01', '2026-01-31', '2026-02-01', '2026-03-15', 10, 0, 3, 'INVITE_ONLY', 90, 500000.00, false, false, 4, NOW(), NOW(), 0),
-- APPROVAL + RECRUITING (승인제 모집중) - 추가 차수
(14, 1, 12, 14, 'React & TypeScript 심화 특별반', 'LIVE', 'FIXED', 40, 'RECRUITING', '2026-01-01', '2026-01-31', '2026-02-05', '2026-03-15', 20, 5, 5, 'APPROVAL', 85, 350000.00, false, false, 3, NOW(), NOW(), 0),
-- FIRST_COME + RECRUITING (수강 기간 먼 미래)
(15, 1, 15, 15, 'SQL 완전 정복 2차', 'ONLINE', 'FIXED', 30, 'RECRUITING', '2026-01-15', '2026-02-15', '2026-03-01', '2026-03-30', 50, 10, 8, 'FIRST_COME', 70, 80000.00, false, true, 4, NOW(), NOW(), 0);

-- ===== 테넌트 2 차수 (차수 ID = 스냅샷 ID로 1:1 매핑) =====
INSERT INTO course_times (id, tenant_id, course_id, snapshot_id, title, delivery_type, duration_type, duration_days, status, enroll_start_date, enroll_end_date, class_start_date, class_end_date, capacity, max_waiting_count, current_enrollment, enrollment_method, min_progress_for_completion, price, is_free, allow_late_enrollment, created_by, created_at, updated_at, version) VALUES
(51, 2, 51, 51, 'Java 기초 입문 1차', 'ONLINE', 'FIXED', 32, 'RECRUITING', '2026-01-01', '2026-01-25', '2026-01-28', '2026-02-28', 30, 5, 15, 'FIRST_COME', 80, 100000.00, false, true, 12, NOW(), NOW(), 0),
(52, 2, 52, 52, 'Spring Framework 실전 1차', 'ONLINE', 'FIXED', 43, 'RECRUITING', '2026-01-05', '2026-01-25', '2026-02-01', '2026-03-15', 25, 5, 10, 'FIRST_COME', 85, 150000.00, false, true, 12, NOW(), NOW(), 0),
(53, 2, 53, 53, '클라우드 네이티브 개발 1차', 'BLENDED', 'FIXED', 55, 'ONGOING', '2025-12-01', '2025-12-31', '2026-01-05', '2026-02-28', 20, 3, 18, 'APPROVAL', 90, 250000.00, false, false, 12, NOW(), NOW(), 0),
(54, 2, 54, 54, '머신러닝 입문 1차', 'ONLINE', 'FIXED', 42, 'ONGOING', '2025-12-15', '2025-12-31', '2026-01-10', '2026-02-20', 40, 10, 35, 'FIRST_COME', 75, 180000.00, false, true, 12, NOW(), NOW(), 0),
(55, 2, 55, 55, '딥러닝 심화 1차', 'ONLINE', 'FIXED', 60, 'DRAFT', '2026-02-01', '2026-02-15', '2026-02-20', '2026-04-20', 30, 5, 0, 'APPROVAL', 80, 300000.00, false, false, 12, NOW(), NOW(), 0),
(56, 2, 51, 56, 'Java 기초 입문 2차', 'ONLINE', 'FIXED', 42, 'CLOSED', '2025-10-01', '2025-10-15', '2025-10-20', '2025-11-30', 30, 5, 28, 'FIRST_COME', 80, 100000.00, false, true, 12, NOW(), NOW(), 0),
-- 테스트용 추가 차수 (테넌트 2)
-- INVITE_ONLY + RECRUITING (선발제 모집중)
(57, 2, 53, 57, '클라우드 네이티브 특별반', 'BLENDED', 'FIXED', 45, 'RECRUITING', '2026-01-01', '2026-01-31', '2026-02-01', '2026-03-15', 10, 0, 3, 'INVITE_ONLY', 90, 400000.00, false, false, 12, NOW(), NOW(), 0),
-- APPROVAL + RECRUITING (승인제 모집중)
(58, 2, 54, 58, '머신러닝 심화 특별반', 'ONLINE', 'FIXED', 40, 'RECRUITING', '2026-01-01', '2026-01-31', '2026-02-05', '2026-03-15', 20, 5, 5, 'APPROVAL', 85, 280000.00, false, false, 12, NOW(), NOW(), 0),
-- FIRST_COME + RECRUITING (수강 기간 먼 미래)
(59, 2, 52, 59, 'Spring Framework 2차', 'ONLINE', 'FIXED', 30, 'RECRUITING', '2026-01-15', '2026-02-15', '2026-03-01', '2026-03-30', 50, 10, 5, 'FIRST_COME', 85, 150000.00, false, true, 12, NOW(), NOW(), 0);

-- ===== 테넌트 3 차수 (차수 ID = 스냅샷 ID로 1:1 매핑) =====
INSERT INTO course_times (id, tenant_id, course_id, snapshot_id, title, delivery_type, duration_type, duration_days, status, enroll_start_date, enroll_end_date, class_start_date, class_end_date, capacity, max_waiting_count, current_enrollment, enrollment_method, min_progress_for_completion, price, is_free, allow_late_enrollment, created_by, created_at, updated_at, version) VALUES
(101, 3, 101, 101, 'React Native 시작하기 1차', 'ONLINE', 'FIXED', 32, 'RECRUITING', '2026-01-01', '2026-01-25', '2026-01-28', '2026-02-28', 25, 5, 12, 'FIRST_COME', 80, 120000.00, false, true, 22, NOW(), NOW(), 0),
(102, 3, 102, 102, 'Flutter 개발 1차', 'ONLINE', 'FIXED', 29, 'RECRUITING', '2026-01-05', '2026-01-28', '2026-02-01', '2026-03-01', 30, 5, 18, 'FIRST_COME', 80, 130000.00, false, true, 22, NOW(), NOW(), 0),
(103, 3, 103, 103, '웹 개발 풀스택 1차', 'BLENDED', 'FIXED', 55, 'ONGOING', '2025-12-01', '2025-12-31', '2026-01-05', '2026-02-28', 35, 8, 30, 'FIRST_COME', 85, 200000.00, false, true, 22, NOW(), NOW(), 0),
(104, 3, 104, 104, '데이터 시각화 1차', 'ONLINE', 'FIXED', 32, 'ONGOING', '2025-12-15', '2025-12-31', '2026-01-10', '2026-02-10', 40, 10, 38, 'FIRST_COME', 70, 80000.00, false, true, 22, NOW(), NOW(), 0),
(105, 3, 101, 105, 'React Native 시작하기 0차', 'ONLINE', 'FIXED', 42, 'CLOSED', '2025-09-01', '2025-09-15', '2025-09-20', '2025-10-31', 20, 3, 18, 'FIRST_COME', 80, 120000.00, false, false, 22, NOW(), NOW(), 0),
(106, 3, 103, 106, '웹 개발 풀스택 0차', 'BLENDED', 'FIXED', 73, 'ARCHIVED', '2025-06-01', '2025-06-15', '2025-06-20', '2025-08-31', 25, 5, 22, 'FIRST_COME', 85, 180000.00, false, true, 22, NOW(), NOW(), 0);
