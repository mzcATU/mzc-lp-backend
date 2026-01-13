-- ============================================
-- CourseTime에 Course, Snapshot 직접 참조 추가
-- Phase 2: 차수별 Snapshot 생성 체계
-- 날짜: 2026-01-13
-- ============================================

-- 1. course_id 컬럼 추가 (Course 직접 참조)
ALTER TABLE course_times
ADD COLUMN course_id BIGINT NULL;

-- 2. snapshot_id 컬럼 추가 (Snapshot 직접 참조)
ALTER TABLE course_times
ADD COLUMN snapshot_id BIGINT NULL;

-- 3. FK 제약조건 추가
ALTER TABLE course_times
ADD CONSTRAINT fk_course_times_course
    FOREIGN KEY (course_id) REFERENCES cm_courses(id);

ALTER TABLE course_times
ADD CONSTRAINT fk_course_times_snapshot
    FOREIGN KEY (snapshot_id) REFERENCES cm_snapshots(id);

-- 4. 인덱스는 엔티티에서 @Index로 관리됨 (중복 방지)
-- idx_course_times_course: tenant_id, course_id
-- idx_course_times_snapshot: tenant_id, snapshot_id
