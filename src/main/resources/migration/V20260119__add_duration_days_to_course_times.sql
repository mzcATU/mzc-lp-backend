-- ============================================
-- CourseTime에 duration_days 컬럼 추가
-- 날짜: 2026-01-19
-- ============================================

-- duration_days 컬럼 추가 (FIXED: 자동 계산, RELATIVE: 필수, UNLIMITED: null)
ALTER TABLE course_times
ADD COLUMN duration_days INT NULL;
