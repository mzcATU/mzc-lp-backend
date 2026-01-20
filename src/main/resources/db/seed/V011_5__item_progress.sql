-- =============================================
-- V011_5: 아이템별 학습 진도 데이터
-- =============================================
-- Enrollment별 SnapshotItem 진도 추적
-- progress_percent: 0 (미완료) 또는 100 (완료)
-- ID 범위: 테넌트 1: 1-500, 테넌트 2: 501-700, 테넌트 3: 701-900
--
-- 참조 관계:
--   Enrollment → CourseTime → Snapshot → SnapshotItem
--   ItemProgress.item_id = SnapshotItem.id (item_type이 있는 학습 콘텐츠만)

-- ===== 테넌트 1 아이템 진도 =====

-- Snapshot#1 (Spring Boot 기초 v1) 학습 아이템: 3,4,5,6
-- Snapshot#2 (React & TypeScript v1) 학습 아이템: 11,12,13
-- Snapshot#3 (Java 프로그래밍 v1) 학습 아이템: 14,15
-- Snapshot#4 (AWS 클라우드 아키텍처 v1) 학습 아이템: 8,9,10
-- Snapshot#5 (Kubernetes 운영 v1) 학습 아이템: 16,17
-- Snapshot#6 (SQL 완전 정복 v1) 학습 아이템: 18,19

-- ----- Spring Boot 기초 3차 (ONGOING, CourseTime#6, Snapshot#1) -----
-- Enrollment#1 (user 101, progress 60%) - 4개 중 2개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(1, 1, 1, 3, 100, 1800, true, NOW() - INTERVAL 8 DAY, 1800, NOW() - INTERVAL 10 DAY, NOW()),
(2, 1, 1, 4, 100, 2400, true, NOW() - INTERVAL 6 DAY, 2400, NOW() - INTERVAL 10 DAY, NOW()),
(3, 1, 1, 5, 0, 1200, false, NULL, 1200, NOW() - INTERVAL 10 DAY, NOW()),
(4, 1, 1, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 10 DAY, NOW());

-- Enrollment#2 (user 102, progress 30%) - 4개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(5, 1, 2, 3, 100, 1800, true, NOW() - INTERVAL 5 DAY, 1800, NOW() - INTERVAL 8 DAY, NOW()),
(6, 1, 2, 4, 0, 800, false, NULL, 800, NOW() - INTERVAL 8 DAY, NOW()),
(7, 1, 2, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 8 DAY, NOW()),
(8, 1, 2, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 8 DAY, NOW());

-- Enrollment#3 (user 107, progress 85%) - 4개 중 3개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(9, 1, 3, 3, 100, 1800, true, NOW() - INTERVAL 10 DAY, 1800, NOW() - INTERVAL 12 DAY, NOW()),
(10, 1, 3, 4, 100, 2400, true, NOW() - INTERVAL 8 DAY, 2400, NOW() - INTERVAL 12 DAY, NOW()),
(11, 1, 3, 5, 100, 3600, true, NOW() - INTERVAL 5 DAY, 3600, NOW() - INTERVAL 12 DAY, NOW()),
(12, 1, 3, 6, 0, 1500, false, NULL, 1500, NOW() - INTERVAL 12 DAY, NOW());

-- Enrollment#4 (user 108, progress 70%) - 4개 중 3개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(13, 1, 4, 3, 100, 1800, true, NOW() - INTERVAL 12 DAY, 1800, NOW() - INTERVAL 15 DAY, NOW()),
(14, 1, 4, 4, 100, 2400, true, NOW() - INTERVAL 10 DAY, 2400, NOW() - INTERVAL 15 DAY, NOW()),
(15, 1, 4, 5, 100, 3600, true, NOW() - INTERVAL 7 DAY, 3600, NOW() - INTERVAL 15 DAY, NOW()),
(16, 1, 4, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 15 DAY, NOW());

-- Enrollment#5 (user 109, progress 20%) - 4개 중 0개 완료 (진행 중)
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(17, 1, 5, 3, 0, 900, false, NULL, 900, NOW() - INTERVAL 5 DAY, NOW()),
(18, 1, 5, 4, 0, 0, false, NULL, 0, NOW() - INTERVAL 5 DAY, NOW()),
(19, 1, 5, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 5 DAY, NOW()),
(20, 1, 5, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 5 DAY, NOW());

-- ----- Spring Boot 기초 4차 (CLOSED, CourseTime#9, Snapshot#1) -----
-- Enrollment#6 (user 103, COMPLETED 100%) - 4개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(21, 1, 6, 3, 100, 1800, true, NOW() - INTERVAL 50 DAY, 1800, NOW() - INTERVAL 60 DAY, NOW()),
(22, 1, 6, 4, 100, 2400, true, NOW() - INTERVAL 45 DAY, 2400, NOW() - INTERVAL 60 DAY, NOW()),
(23, 1, 6, 5, 100, 3600, true, NOW() - INTERVAL 40 DAY, 3600, NOW() - INTERVAL 60 DAY, NOW()),
(24, 1, 6, 6, 100, 4200, true, NOW() - INTERVAL 35 DAY, 4200, NOW() - INTERVAL 60 DAY, NOW());

-- Enrollment#7 (user 104, COMPLETED 100%) - 4개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(25, 1, 7, 3, 100, 1800, true, NOW() - INTERVAL 48 DAY, 1800, NOW() - INTERVAL 55 DAY, NOW()),
(26, 1, 7, 4, 100, 2400, true, NOW() - INTERVAL 43 DAY, 2400, NOW() - INTERVAL 55 DAY, NOW()),
(27, 1, 7, 5, 100, 3600, true, NOW() - INTERVAL 38 DAY, 3600, NOW() - INTERVAL 55 DAY, NOW()),
(28, 1, 7, 6, 100, 4200, true, NOW() - INTERVAL 33 DAY, 4200, NOW() - INTERVAL 55 DAY, NOW());

-- Enrollment#8 (user 110, CANCELLED 40%) - 4개 중 1개 완료 후 취소
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(29, 1, 8, 3, 100, 1800, true, NOW() - INTERVAL 45 DAY, 1800, NOW() - INTERVAL 50 DAY, NOW()),
(30, 1, 8, 4, 0, 600, false, NULL, 600, NOW() - INTERVAL 50 DAY, NOW()),
(31, 1, 8, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 50 DAY, NOW()),
(32, 1, 8, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 50 DAY, NOW());

-- Enrollment#9 (user 111, COMPLETED 100%) - 4개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(33, 1, 9, 3, 100, 1800, true, NOW() - INTERVAL 52 DAY, 1800, NOW() - INTERVAL 58 DAY, NOW()),
(34, 1, 9, 4, 100, 2400, true, NOW() - INTERVAL 47 DAY, 2400, NOW() - INTERVAL 58 DAY, NOW()),
(35, 1, 9, 5, 100, 3600, true, NOW() - INTERVAL 42 DAY, 3600, NOW() - INTERVAL 58 DAY, NOW()),
(36, 1, 9, 6, 100, 4200, true, NOW() - INTERVAL 37 DAY, 4200, NOW() - INTERVAL 58 DAY, NOW());

-- Enrollment#10 (user 112, COMPLETED 100%) - 4개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(37, 1, 10, 3, 100, 1800, true, NOW() - INTERVAL 46 DAY, 1800, NOW() - INTERVAL 52 DAY, NOW()),
(38, 1, 10, 4, 100, 2400, true, NOW() - INTERVAL 41 DAY, 2400, NOW() - INTERVAL 52 DAY, NOW()),
(39, 1, 10, 5, 100, 3600, true, NOW() - INTERVAL 36 DAY, 3600, NOW() - INTERVAL 52 DAY, NOW()),
(40, 1, 10, 6, 100, 4200, true, NOW() - INTERVAL 31 DAY, 4200, NOW() - INTERVAL 52 DAY, NOW());

-- ----- Java 프로그래밍 마스터 1차 (ONGOING, CourseTime#7, Snapshot#3) -----
-- Enrollment#11 (user 105, progress 45%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(41, 1, 11, 14, 100, 3600, true, NOW() - INTERVAL 15 DAY, 3600, NOW() - INTERVAL 20 DAY, NOW()),
(42, 1, 11, 15, 0, 1800, false, NULL, 1800, NOW() - INTERVAL 20 DAY, NOW());

-- Enrollment#12 (user 106, progress 55%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(43, 1, 12, 14, 100, 3600, true, NOW() - INTERVAL 12 DAY, 3600, NOW() - INTERVAL 18 DAY, NOW()),
(44, 1, 12, 15, 0, 2700, false, NULL, 2700, NOW() - INTERVAL 18 DAY, NOW());

-- Enrollment#13 (user 113, progress 70%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(45, 1, 13, 14, 100, 3600, true, NOW() - INTERVAL 18 DAY, 3600, NOW() - INTERVAL 22 DAY, NOW()),
(46, 1, 13, 15, 0, 3800, false, NULL, 3800, NOW() - INTERVAL 22 DAY, NOW());

-- Enrollment#14 (user 114, progress 80%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(47, 1, 14, 14, 100, 3600, true, NOW() - INTERVAL 20 DAY, 3600, NOW() - INTERVAL 25 DAY, NOW()),
(48, 1, 14, 15, 0, 4300, false, NULL, 4300, NOW() - INTERVAL 25 DAY, NOW());

-- ----- Kubernetes 운영 실무 1차 (ONGOING, CourseTime#8, Snapshot#5) -----
-- Enrollment#15 (user 115, progress 50%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(49, 1, 15, 16, 100, 4200, true, NOW() - INTERVAL 20 DAY, 4200, NOW() - INTERVAL 30 DAY, NOW()),
(50, 1, 15, 17, 0, 1000, false, NULL, 1000, NOW() - INTERVAL 30 DAY, NOW());

-- Enrollment#16 (user 116, progress 65%) - 2개 중 1개 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(51, 1, 16, 16, 100, 4200, true, NOW() - INTERVAL 22 DAY, 4200, NOW() - INTERVAL 28 DAY, NOW()),
(52, 1, 16, 17, 0, 2340, false, NULL, 2340, NOW() - INTERVAL 28 DAY, NOW());

-- ----- SQL 완전 정복 1차 (CLOSED, CourseTime#10, Snapshot#6) -----
-- Enrollment#17 (user 117, COMPLETED 100%) - 2개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(53, 1, 17, 18, 100, 1800, true, NOW() - INTERVAL 35 DAY, 1800, NOW() - INTERVAL 45 DAY, NOW()),
(54, 1, 17, 19, 100, 2700, true, NOW() - INTERVAL 30 DAY, 2700, NOW() - INTERVAL 45 DAY, NOW());

-- Enrollment#18 (user 118, COMPLETED 100%) - 2개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(55, 1, 18, 18, 100, 1800, true, NOW() - INTERVAL 33 DAY, 1800, NOW() - INTERVAL 43 DAY, NOW()),
(56, 1, 18, 19, 100, 2700, true, NOW() - INTERVAL 28 DAY, 2700, NOW() - INTERVAL 43 DAY, NOW());

-- Enrollment#19 (user 119, COMPLETED 100%) - 2개 모두 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(57, 1, 19, 18, 100, 1800, true, NOW() - INTERVAL 32 DAY, 1800, NOW() - INTERVAL 42 DAY, NOW()),
(58, 1, 19, 19, 100, 2700, true, NOW() - INTERVAL 27 DAY, 2700, NOW() - INTERVAL 42 DAY, NOW());

-- ----- Spring Boot 기초 2차 (RECRUITING, CourseTime#3, Snapshot#1) -----
-- Enrollment#20,21,22 - 모집 중이라 진도 0%
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(59, 1, 20, 3, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW()),
(60, 1, 20, 4, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW()),
(61, 1, 20, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW()),
(62, 1, 20, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW()),
(63, 1, 21, 3, 0, 0, false, NULL, 0, NOW() - INTERVAL 2 DAY, NOW()),
(64, 1, 21, 4, 0, 0, false, NULL, 0, NOW() - INTERVAL 2 DAY, NOW()),
(65, 1, 21, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 2 DAY, NOW()),
(66, 1, 21, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 2 DAY, NOW()),
(67, 1, 22, 3, 0, 0, false, NULL, 0, NOW() - INTERVAL 1 DAY, NOW()),
(68, 1, 22, 4, 0, 0, false, NULL, 0, NOW() - INTERVAL 1 DAY, NOW()),
(69, 1, 22, 5, 0, 0, false, NULL, 0, NOW() - INTERVAL 1 DAY, NOW()),
(70, 1, 22, 6, 0, 0, false, NULL, 0, NOW() - INTERVAL 1 DAY, NOW());


-- ===== 테넌트 2 아이템 진도 =====

-- Snapshot#51 (Java 기초 입문 v1) 학습 아이템: 51
-- Snapshot#52 (Spring Framework 실전 v1) 학습 아이템: 52
-- Snapshot#53 (클라우드 네이티브 개발 v1) 학습 아이템: 53
-- Snapshot#54 (머신러닝 입문 v1) 학습 아이템: 54

-- ----- Java 기초 입문 1차 (RECRUITING, CourseTime#51, Snapshot#51) -----
-- Enrollment#201,202,203 - 모집 중이라 진도 0%
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(501, 2, 201, 51, 0, 0, false, NULL, 0, NOW() - INTERVAL 5 DAY, NOW()),
(502, 2, 202, 51, 0, 0, false, NULL, 0, NOW() - INTERVAL 4 DAY, NOW()),
(503, 2, 203, 51, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW());

-- ----- 클라우드 네이티브 개발 1차 (ONGOING, CourseTime#53, Snapshot#53) -----
-- Enrollment#204 (progress 45%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(504, 2, 204, 53, 0, 1350, false, NULL, 1350, NOW() - INTERVAL 15 DAY, NOW());

-- Enrollment#205 (progress 50%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(505, 2, 205, 53, 0, 1500, false, NULL, 1500, NOW() - INTERVAL 14 DAY, NOW());

-- Enrollment#206 (progress 60%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(506, 2, 206, 53, 0, 1800, false, NULL, 1800, NOW() - INTERVAL 12 DAY, NOW());

-- ----- 머신러닝 입문 1차 (ONGOING, CourseTime#54, Snapshot#54) -----
-- Enrollment#207 (progress 35%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(507, 2, 207, 54, 0, 1470, false, NULL, 1470, NOW() - INTERVAL 10 DAY, NOW());

-- Enrollment#208 (progress 25%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(508, 2, 208, 54, 0, 1050, false, NULL, 1050, NOW() - INTERVAL 8 DAY, NOW());

-- ----- Java 기초 입문 2차 (CLOSED, CourseTime#56, Snapshot#51) -----
-- Enrollment#209 (COMPLETED 100%) - 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(509, 2, 209, 51, 100, 2700, true, NOW() - INTERVAL 30 DAY, 2700, NOW() - INTERVAL 50 DAY, NOW());

-- Enrollment#210 (COMPLETED 100%) - 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(510, 2, 210, 51, 100, 2700, true, NOW() - INTERVAL 28 DAY, 2700, NOW() - INTERVAL 48 DAY, NOW());


-- ===== 테넌트 3 아이템 진도 =====

-- Snapshot#101 (React Native 시작하기 v1) 학습 아이템: 101
-- Snapshot#102 (Flutter 개발 v1) 학습 아이템: 102
-- Snapshot#103 (웹 개발 풀스택 v1) 학습 아이템: 103
-- Snapshot#104 (데이터 시각화 v1) 학습 아이템: 104

-- ----- React Native 시작하기 1차 (RECRUITING, CourseTime#101, Snapshot#101) -----
-- Enrollment#401,402 - 모집 중이라 진도 0%
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(701, 3, 401, 101, 0, 0, false, NULL, 0, NOW() - INTERVAL 5 DAY, NOW()),
(702, 3, 402, 101, 0, 0, false, NULL, 0, NOW() - INTERVAL 4 DAY, NOW());

-- ----- Flutter 개발 1차 (RECRUITING, CourseTime#102, Snapshot#102) -----
-- Enrollment#403,404 - 모집 중이라 진도 0%
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(703, 3, 403, 102, 0, 0, false, NULL, 0, NOW() - INTERVAL 3 DAY, NOW()),
(704, 3, 404, 102, 0, 0, false, NULL, 0, NOW() - INTERVAL 2 DAY, NOW());

-- ----- 웹 개발 풀스택 1차 (ONGOING, CourseTime#103, Snapshot#103) -----
-- Enrollment#405 (progress 40%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(705, 3, 405, 103, 0, 1800, false, NULL, 1800, NOW() - INTERVAL 15 DAY, NOW());

-- Enrollment#406 (progress 50%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(706, 3, 406, 103, 0, 2250, false, NULL, 2250, NOW() - INTERVAL 14 DAY, NOW());

-- Enrollment#407 (progress 55%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(707, 3, 407, 103, 0, 2475, false, NULL, 2475, NOW() - INTERVAL 12 DAY, NOW());

-- ----- 데이터 시각화 1차 (ONGOING, CourseTime#104, Snapshot#104) -----
-- Enrollment#408 (progress 30%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(708, 3, 408, 104, 0, 810, false, NULL, 810, NOW() - INTERVAL 10 DAY, NOW());

-- Enrollment#409 (progress 40%) - 진행 중
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(709, 3, 409, 104, 0, 1080, false, NULL, 1080, NOW() - INTERVAL 8 DAY, NOW());

-- ----- React Native 시작하기 0차 (CLOSED, CourseTime#105, Snapshot#101) -----
-- Enrollment#410 (COMPLETED 100%) - 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(710, 3, 410, 101, 100, 2400, true, NOW() - INTERVAL 40 DAY, 2400, NOW() - INTERVAL 60 DAY, NOW());

-- Enrollment#411 (COMPLETED 100%) - 완료
INSERT INTO sis_item_progress (id, tenant_id, enrollment_id, item_id, progress_percent, watched_seconds, completed, completed_at, last_position_seconds, created_at, updated_at) VALUES
(711, 3, 411, 101, 100, 2400, true, NOW() - INTERVAL 38 DAY, 2400, NOW() - INTERVAL 58 DAY, NOW());
