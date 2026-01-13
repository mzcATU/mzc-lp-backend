-- ============================================
-- Course status 마이그레이션: PUBLISHED -> READY
-- 적용 대상: cm_courses 테이블
-- 날짜: 2026-01-13
-- 이슈: #368
-- ============================================

-- 기존 PUBLISHED 상태를 READY로 변경
UPDATE cm_courses SET status = 'READY' WHERE status = 'PUBLISHED';
