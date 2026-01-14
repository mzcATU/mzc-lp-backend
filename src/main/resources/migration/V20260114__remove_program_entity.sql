-- ============================================
-- Program 엔티티 제거
-- Phase 3: Program 제거, Course 직접 참조
-- 날짜: 2026-01-14
-- ============================================

-- 1. course_times 테이블에서 program_id 관련 제거
-- 이전 마이그레이션에서 course_id가 추가되었으므로 program_id는 더 이상 필요 없음
ALTER TABLE course_times
DROP COLUMN IF EXISTS program_id;

-- 2. certificates 테이블에서 program 관련 컬럼 제거
ALTER TABLE certificates
DROP COLUMN IF EXISTS program_id;

ALTER TABLE certificates
DROP COLUMN IF EXISTS program_title;

-- 3. cm_programs 테이블 삭제
-- 먼저 FK 제약조건이 있다면 삭제
SET @var := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
             WHERE CONSTRAINT_SCHEMA = DATABASE()
             AND TABLE_NAME = 'course_times'
             AND CONSTRAINT_NAME = 'fk_course_times_program');
SET @sql := IF(@var > 0, 'ALTER TABLE course_times DROP FOREIGN KEY fk_course_times_program', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cm_programs 테이블 삭제 (존재하는 경우)
DROP TABLE IF EXISTS cm_programs;

-- 4. roadmap_programs 테이블은 유지 (Roadmap 기능 비활성화 상태)
-- 추후 Course 기반 Roadmap 재설계 시 마이그레이션 예정
-- program_id 컬럼은 유지하되 사용하지 않음

-- 5. 인덱스 정리 (program_id 관련 인덱스 삭제)
-- MySQL에서 인덱스 존재 여부 확인 후 삭제
SET @var := (SELECT COUNT(*) FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME = 'course_times'
             AND INDEX_NAME = 'idx_course_times_program');
SET @sql := IF(@var > 0, 'ALTER TABLE course_times DROP INDEX idx_course_times_program', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
