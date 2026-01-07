-- ============================================
-- Course status 필드 추가 마이그레이션
-- 적용 대상: cm_courses 테이블
-- 날짜: 2026-01-07
-- 이슈: #333
-- ============================================

-- 1. status 컬럼 추가
-- Hibernate DDL Auto가 컬럼을 자동 생성하지만, 명시적 스크립트 보관
ALTER TABLE cm_courses
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 2. 기존 데이터 마이그레이션
-- 정책 결정: 모든 기존 강의를 DRAFT로 유지 (안전)
UPDATE cm_courses SET status = 'DRAFT' WHERE status IS NULL;

-- 3. 인덱스 추가 (status 필터링 성능 최적화)
CREATE INDEX IF NOT EXISTS idx_course_status ON cm_courses(status);
CREATE INDEX IF NOT EXISTS idx_course_tenant_status ON cm_courses(tenant_id, status);
