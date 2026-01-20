-- ============================================
-- CourseTime에 duration_type, schedule 관련 컬럼 추가
-- 날짜: 2026-01-20
-- 이슈: #415
-- ============================================

-- duration_type 컬럼 추가 (FIXED, RELATIVE, UNLIMITED)
ALTER TABLE course_times
ADD COLUMN duration_type VARCHAR(20) NOT NULL DEFAULT 'FIXED';

-- schedule_days_of_week 컬럼 추가 (수업 요일)
ALTER TABLE course_times
ADD COLUMN schedule_days_of_week JSON NULL;

-- schedule_start_time 컬럼 추가 (수업 시작 시간)
ALTER TABLE course_times
ADD COLUMN schedule_start_time TIME NULL;

-- schedule_end_time 컬럼 추가 (수업 종료 시간)
ALTER TABLE course_times
ADD COLUMN schedule_end_time TIME NULL;

-- class_end_date NULL 허용으로 변경 (UNLIMITED 타입용)
ALTER TABLE course_times
MODIFY COLUMN class_end_date DATE NULL;
