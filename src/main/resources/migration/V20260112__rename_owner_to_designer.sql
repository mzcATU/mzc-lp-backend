-- ============================================
-- CourseRole OWNER → DESIGNER 마이그레이션
-- 적용 대상: user_course_roles 테이블
-- 날짜: 2026-01-12
-- ============================================

-- 1. OWNER 역할을 DESIGNER로 변경
-- 기존 CourseRole.OWNER가 있던 레코드를 DESIGNER로 업데이트
UPDATE user_course_roles
SET role = 'DESIGNER'
WHERE role = 'OWNER';

-- 2. 마이그레이션 결과 확인용 쿼리 (실행 후 삭제 가능)
-- SELECT role, COUNT(*) as count FROM user_course_roles GROUP BY role;
