-- =============================================
-- [필수] 외래 키 검사 비활성화 (순서 무관하게 삭제하기 위함)
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 테넌트 데이터 (3개 테넌트)
-- =============================================
-- 테넌트 1: 기본 테넌트
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 테넌트 2: A사 (기업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 2, 'company-a', 'A사 교육센터', 'B2B', 'ACTIVE', 'PRO', 'company-a', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 2);

-- 테넌트 3: B사 (스타트업 교육)
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 3, 'company-b', 'B사 아카데미', 'B2B', 'ACTIVE', 'BASIC', 'company-b', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 3);

-- =============================================
-- 2. 기존 데이터 삭제 (Clean Up)
--    외래 키 검사가 꺼져 있으므로 순서 상관없이 삭제 가능
-- =============================================
-- 레거시 컬럼(course_id)이 남아있을 수 있어 테이블 재생성
DROP TABLE IF EXISTS cm_wishlist_items;
CREATE TABLE cm_wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    course_time_id BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlist_user_course_time (tenant_id, user_id, course_time_id),
    INDEX idx_wishlist_user (tenant_id, user_id),
    INDEX idx_wishlist_course_time (tenant_id, course_time_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS cart_items;
CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    course_time_id BIGINT NOT NULL,
    added_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_course_time (tenant_id, user_id, course_time_id),
    INDEX idx_cart_tenant_user (tenant_id, user_id),
    INDEX idx_cart_added_at (added_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- program_id → course_id 스키마 변경으로 인해 테이블 재생성
DROP TABLE IF EXISTS course_times;
CREATE TABLE course_times (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    version BIGINT,
    course_id BIGINT,
    snapshot_id BIGINT,
    title VARCHAR(200) NOT NULL,
    delivery_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    enroll_start_date DATE NOT NULL,
    enroll_end_date DATE NOT NULL,
    class_start_date DATE NOT NULL,
    class_end_date DATE NOT NULL,
    capacity INT,
    max_waiting_count INT,
    current_enrollment INT NOT NULL DEFAULT 0,
    enrollment_method VARCHAR(20) NOT NULL,
    min_progress_for_completion INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_free BOOLEAN NOT NULL DEFAULT FALSE,
    location_info JSON,
    allow_late_enrollment BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_course_times_status (tenant_id, status),
    INDEX idx_course_times_course (tenant_id, course_id),
    INDEX idx_course_times_snapshot (tenant_id, snapshot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 테넌트 1, 2, 3 모두 삭제
DELETE FROM cm_course_reviews WHERE tenant_id IN (1, 2, 3);
DELETE FROM community_posts WHERE tenant_id IN (1, 2, 3);
DELETE FROM roadmap_programs WHERE roadmap_id IN (SELECT id FROM roadmaps WHERE tenant_id IN (1, 2, 3));
DELETE FROM roadmaps WHERE tenant_id IN (1, 2, 3);
DELETE FROM sis_enrollments WHERE tenant_id IN (1, 2, 3);
DELETE FROM iis_instructor_assignments WHERE tenant_id IN (1, 2, 3);
DELETE FROM user_course_roles WHERE tenant_id IN (1, 2, 3);
-- DELETE FROM course_times는 DROP TABLE로 대체됨
-- DELETE FROM cm_programs는 테이블이 제거되어 삭제됨
DELETE FROM cm_snapshot_relations WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_snapshot_items WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_snapshot_los WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_snapshots WHERE tenant_id IN (1, 2, 3);
DELETE FROM content WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_course_items WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_course_tags WHERE course_id IN (SELECT id FROM cm_courses WHERE tenant_id IN (1, 2, 3));
DELETE FROM cm_courses WHERE tenant_id IN (1, 2, 3);
DELETE FROM cm_categories WHERE tenant_id IN (1, 2, 3);
DELETE FROM users WHERE tenant_id IN (1, 2, 3);
DELETE FROM departments WHERE tenant_id IN (1, 2, 3);

-- =============================================
-- 3. 부서 데이터 INSERT (테넌트별)
-- =============================================

-- ===== 테넌트 1 (기본 테넌트) 부서들 =====
INSERT INTO departments (tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(1, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(1, '마케팅팀', 'MKT', '마케팅 및 홍보 부서', NULL, 2, true, NOW(), NOW()),
(1, '인사팀', 'HR', '인사 관리 부서', NULL, 3, true, NOW(), NOW()),
(1, '영업팀', 'SALES', '영업 및 고객 관리 부서', NULL, 4, true, NOW(), NOW()),
(1, '디자인팀', 'DESIGN', 'UI/UX 및 그래픽 디자인 부서', NULL, 5, true, NOW(), NOW()),
(1, '경영지원팀', 'BIZ', '경영 지원 부서', NULL, 6, true, NOW(), NOW());

-- ===== 테넌트 2 (A사) 부서들 =====
INSERT INTO departments (tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(2, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(2, '기획팀', 'PLAN', '서비스 기획 부서', NULL, 2, true, NOW(), NOW()),
(2, '인사팀', 'HR', '인사 관리 부서', NULL, 3, true, NOW(), NOW()),
(2, '재무팀', 'FIN', '재무 관리 부서', NULL, 4, true, NOW(), NOW()),
(2, '품질관리팀', 'QA', '품질 보증 부서', NULL, 5, true, NOW(), NOW());

-- ===== 테넌트 3 (B사) 부서들 =====
INSERT INTO departments (tenant_id, name, code, description, parent_id, sort_order, is_active, created_at, updated_at) VALUES
(3, '개발팀', 'DEV', '소프트웨어 개발 부서', NULL, 1, true, NOW(), NOW()),
(3, '마케팅팀', 'MKT', '마케팅 부서', NULL, 2, true, NOW(), NOW()),
(3, '운영팀', 'OPS', '서비스 운영 부서', NULL, 3, true, NOW(), NOW()),
(3, '고객지원팀', 'CS', '고객 지원 부서', NULL, 4, true, NOW(), NOW());

-- =============================================
-- 4. 사용자 데이터 INSERT
-- 비밀번호: 1q2w3e4r! (BCrypt 암호화)
-- =============================================

-- ===== 시스템 관리자 (전체 1명) =====
INSERT INTO users (tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(1, 'sysadmin@mzc.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '시스템관리자', '010-0000-0001', NULL, NULL, 'SYSTEM_ADMIN', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 1 (기본 테넌트) 사용자들 =====
INSERT INTO users (tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
-- 테넌트 관리자
(1, 'admin@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '기본테넌트관리자', '010-1000-0001', '경영지원팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
-- 운영자
(1, 'operator1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '운영자1', '010-1000-0002', '경영지원팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
(1, 'operator2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '운영자2', '010-1000-0003', '경영지원팀', '대리', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
-- 설계자
(1, 'designer1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '설계자1', '010-1000-0004', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(1, 'designer2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '설계자2', '010-1000-0005', '개발팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(1, 'designer3@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '설계자3', '010-1000-0006', '디자인팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
-- 강의 개설자 (테스트용)
(1, 'creator@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강의개설자', '010-1000-0007', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
-- 일반 사용자 50명 (user1 ~ user50) - 부서/직급 분배
-- 개발팀 (15명)
(1, 'user1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김민준', '010-1001-0001', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이서연', '010-1001-0002', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user3@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박지호', '010-1001-0003', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user4@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '최수아', '010-1001-0004', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user5@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '정우진', '010-1001-0005', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user6@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강하은', '010-1001-0006', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user7@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '윤준서', '010-1001-0007', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user8@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '장예은', '010-1001-0008', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user9@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '임도현', '010-1001-0009', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user10@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '한소희', '010-1001-0010', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user11@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '오시우', '010-1001-0011', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user12@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '서유진', '010-1001-0012', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user13@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '신현우', '010-1001-0013', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user14@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '권민서', '010-1001-0014', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user15@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '황지안', '010-1001-0015', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 마케팅팀 (10명)
(1, 'user16@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '안서준', '010-1001-0016', '마케팅팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user17@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '송예린', '010-1001-0017', '마케팅팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user18@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '전민재', '010-1001-0018', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user19@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '홍수빈', '010-1001-0019', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user20@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '유하준', '010-1001-0020', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user21@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '조아린', '010-1001-0021', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user22@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '백승민', '010-1001-0022', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user23@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '노지원', '010-1001-0023', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user24@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '하태민', '010-1001-0024', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user25@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '문채원', '010-1001-0025', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 인사팀 (8명)
(1, 'user26@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '양현준', '010-1001-0026', '인사팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user27@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '배나은', '010-1001-0027', '인사팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user28@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '남지훈', '010-1001-0028', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user29@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '심유나', '010-1001-0029', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user30@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '곽동현', '010-1001-0030', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user31@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '탁서영', '010-1001-0031', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user32@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '엄재윤', '010-1001-0032', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user33@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '표다인', '010-1001-0033', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 영업팀 (10명)
(1, 'user34@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '채성호', '010-1001-0034', '영업팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user35@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '원미래', '010-1001-0035', '영업팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user36@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '길준혁', '010-1001-0036', '영업팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user37@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '방소율', '010-1001-0037', '영업팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user38@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '추건우', '010-1001-0038', '영업팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user39@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '봉하늘', '010-1001-0039', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user40@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '석진우', '010-1001-0040', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user41@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '편가영', '010-1001-0041', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user42@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '선우혁', '010-1001-0042', '영업팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user43@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '독고윤', '010-1001-0043', '영업팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 디자인팀 (7명)
(1, 'user44@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '빈세라', '010-1001-0044', '디자인팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user45@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '피태양', '010-1001-0045', '디자인팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user46@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '공보람', '010-1001-0046', '디자인팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user47@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '국승리', '010-1001-0047', '디자인팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user48@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '도경민', '010-1001-0048', '디자인팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user49@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '류다온', '010-1001-0049', '디자인팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1, 'user50@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '마준영', '010-1001-0050', '디자인팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 2 (A사 교육센터) 사용자들 =====
INSERT INTO users (tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
-- 테넌트 관리자
(2, 'admin@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A사관리자', '010-2000-0001', '인사팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
-- 운영자
(2, 'operator1@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A사운영자1', '010-2000-0002', '인사팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
-- 설계자
(2, 'designer1@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A사설계자1', '010-2000-0003', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(2, 'designer2@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A사설계자2', '010-2000-0004', '기획팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
-- 일반 사용자 50명 - 부서/직급 분배
-- 개발팀 (15명)
(2, 'user1@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A김철수', '010-2001-0001', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user2@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A이영희', '010-2001-0002', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user3@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A박민수', '010-2001-0003', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user4@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A최지우', '010-2001-0004', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user5@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A정현아', '010-2001-0005', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user6@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A강태웅', '010-2001-0006', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user7@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A윤서진', '010-2001-0007', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user8@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A장미래', '010-2001-0008', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user9@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A임현수', '010-2001-0009', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user10@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A한소연', '010-2001-0010', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user11@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A오준혁', '010-2001-0011', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user12@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A서다은', '010-2001-0012', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user13@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A신우진', '010-2001-0013', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user14@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A권나라', '010-2001-0014', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user15@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A황민호', '010-2001-0015', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 기획팀 (12명)
(2, 'user16@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A안예진', '010-2001-0016', '기획팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user17@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A송태현', '010-2001-0017', '기획팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user18@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A전수빈', '010-2001-0018', '기획팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user19@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A홍지민', '010-2001-0019', '기획팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user20@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A유하진', '010-2001-0020', '기획팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user21@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A조민석', '010-2001-0021', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user22@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A백수진', '010-2001-0022', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user23@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A노건우', '010-2001-0023', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user24@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A하민지', '010-2001-0024', '기획팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user25@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A문성훈', '010-2001-0025', '기획팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user26@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A양채린', '010-2001-0026', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user27@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A배현준', '010-2001-0027', '기획팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
-- 인사팀 (8명)
(2, 'user28@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A남가영', '010-2001-0028', '인사팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user29@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A심동우', '010-2001-0029', '인사팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user30@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A곽예림', '010-2001-0030', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user31@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A탁승우', '010-2001-0031', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user32@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A엄소영', '010-2001-0032', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user33@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A표재윤', '010-2001-0033', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user34@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A채지안', '010-2001-0034', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user35@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A원태윤', '010-2001-0035', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 재무팀 (8명)
(2, 'user36@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A길나윤', '010-2001-0036', '재무팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user37@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A방준서', '010-2001-0037', '재무팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user38@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A추아린', '010-2001-0038', '재무팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user39@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A봉현우', '010-2001-0039', '재무팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user40@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A석다인', '010-2001-0040', '재무팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user41@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A편성민', '010-2001-0041', '재무팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user42@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A선우민', '010-2001-0042', '재무팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user43@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A독고연', '010-2001-0043', '재무팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 품질관리팀 (7명)
(2, 'user44@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A빈태호', '010-2001-0044', '품질관리팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user45@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A피소라', '010-2001-0045', '품질관리팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user46@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A공민재', '010-2001-0046', '품질관리팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user47@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A국하늘', '010-2001-0047', '품질관리팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user48@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A도영우', '010-2001-0048', '품질관리팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user49@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A류민서', '010-2001-0049', '품질관리팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'user50@company-a.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'A마서연', '010-2001-0050', '품질관리팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 3 (B사 아카데미) 사용자들 =====
INSERT INTO users (tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
-- 테넌트 관리자
(3, 'admin@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B사관리자', '010-3000-0001', '운영팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
-- 운영자
(3, 'operator1@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B사운영자1', '010-3000-0002', '운영팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
-- 설계자
(3, 'designer1@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B사설계자1', '010-3000-0003', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(3, 'designer2@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B사설계자2', '010-3000-0004', '개발팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
-- 일반 사용자 50명 (4개 부서 분배: 개발팀, 마케팅팀, 운영팀, 고객지원팀)
-- 개발팀 (15명)
(3, 'user1@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B박준혁', '010-3001-0001', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user2@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B김서아', '010-3001-0002', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user3@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B이도윤', '010-3001-0003', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user4@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B최하은', '010-3001-0004', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user5@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B정시우', '010-3001-0005', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user6@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B강예은', '010-3001-0006', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user7@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B윤지호', '010-3001-0007', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user8@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B장수아', '010-3001-0008', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user9@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B임우진', '010-3001-0009', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user10@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B한서연', '010-3001-0010', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user11@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B오민준', '010-3001-0011', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user12@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B서지안', '010-3001-0012', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user13@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B신현우', '010-3001-0013', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user14@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B권예린', '010-3001-0014', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user15@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B황태민', '010-3001-0015', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 마케팅팀 (12명)
(3, 'user16@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B안나은', '010-3001-0016', '마케팅팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user17@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B송준서', '010-3001-0017', '마케팅팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user18@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B전아린', '010-3001-0018', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user19@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B홍승민', '010-3001-0019', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user20@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B유지원', '010-3001-0020', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user21@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B조하준', '010-3001-0021', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user22@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B백민서', '010-3001-0022', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user23@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B노태현', '010-3001-0023', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user24@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B하수빈', '010-3001-0024', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user25@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B문지민', '010-3001-0025', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user26@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B양하진', '010-3001-0026', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user27@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B배민석', '010-3001-0027', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 운영팀 (12명)
(3, 'user28@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B남수진', '010-3001-0028', '운영팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user29@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B심건우', '010-3001-0029', '운영팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user30@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B곽민지', '010-3001-0030', '운영팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user31@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B탁성훈', '010-3001-0031', '운영팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user32@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B엄채린', '010-3001-0032', '운영팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user33@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B표현준', '010-3001-0033', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user34@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B채가영', '010-3001-0034', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user35@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B원동우', '010-3001-0035', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user36@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B길예림', '010-3001-0036', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user37@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B방승우', '010-3001-0037', '운영팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user38@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B추소영', '010-3001-0038', '운영팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user39@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B봉재윤', '010-3001-0039', '운영팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 고객지원팀 (11명)
(3, 'user40@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B석지안', '010-3001-0040', '고객지원팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user41@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B편태윤', '010-3001-0041', '고객지원팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user42@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B선우나', '010-3001-0042', '고객지원팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user43@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B독고준', '010-3001-0043', '고객지원팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user44@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B빈아린', '010-3001-0044', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user45@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B피현우', '010-3001-0045', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user46@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B공다인', '010-3001-0046', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user47@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B국성호', '010-3001-0047', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user48@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B도미래', '010-3001-0048', '고객지원팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user49@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B류준영', '010-3001-0049', '고객지원팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'user50@company-b.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', 'B마다온', '010-3001-0050', '고객지원팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW());

-- =============================================
-- 4. 카테고리 데이터 INSERT (프론트엔드와 ID 일치 필요)
-- =============================================
INSERT INTO cm_categories (id, tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(1, 1, '개발', 'dev', 1, true, NOW(), NOW()),
(2, 1, 'AI', 'ai', 2, true, NOW(), NOW()),
(3, 1, '데이터', 'data', 3, true, NOW(), NOW()),
(4, 1, '디자인', 'design', 4, true, NOW(), NOW()),
(5, 1, '비즈니스', 'business', 5, true, NOW(), NOW()),
(6, 1, '마케팅', 'marketing', 6, true, NOW(), NOW()),
(7, 1, '외국어', 'language', 7, true, NOW(), NOW());

-- =============================================
-- 5. 코스 데이터 INSERT (카테고리 연결)
-- =============================================
INSERT INTO cm_courses (tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version) VALUES
-- 개발 카테고리 (id=1)
(1, 'Spring Boot 기초', 'Spring Boot 프레임워크의 기본 개념과 실습', 'BEGINNER', 'ONLINE', 20,
 (SELECT id FROM cm_categories WHERE code = 'dev' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, 'React & TypeScript', 'React와 TypeScript를 활용한 프론트엔드 개발', 'ADVANCED', 'ONLINE', 30,
 (SELECT id FROM cm_categories WHERE code = 'dev' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, 'Java 프로그래밍', '자바 언어의 핵심 개념부터 고급 기능까지', 'INTERMEDIATE', 'ONLINE', 45,
 (SELECT id FROM cm_categories WHERE code = 'dev' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 35 DAY, NOW(), 0),
(1, 'Kubernetes 운영', '쿠버네티스 클러스터 운영 및 모니터링', 'ADVANCED', 'BLENDED', 50,
 (SELECT id FROM cm_categories WHERE code = 'dev' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 40 DAY, NOW(), 0),
(1, 'DevOps 엔지니어링', 'CI/CD 파이프라인 구축과 컨테이너 오케스트레이션', 'INTERMEDIATE', 'OFFLINE', 35,
 (SELECT id FROM cm_categories WHERE code = 'dev' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 18 DAY, NOW(), 0),
-- AI 카테고리 (id=2)
(1, 'ChatGPT 활용법', 'ChatGPT를 업무에 활용하는 방법', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'ai' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 28 DAY, NOW(), 0),
-- 데이터 카테고리 (id=3)
(1, 'SQL 완전 정복', '관계형 데이터베이스와 SQL 쿼리 작성', 'BEGINNER', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'data' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, 'Python 데이터 분석', 'Pandas, NumPy를 활용한 데이터 분석', 'BEGINNER', 'ONLINE', 25,
 (SELECT id FROM cm_categories WHERE code = 'data' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 15 DAY, NOW(), 0),
-- 비즈니스 카테고리 (id=5)
(1, 'AWS 클라우드 아키텍처', 'AWS 서비스를 활용한 클라우드 인프라 설계', 'INTERMEDIATE', 'BLENDED', 40,
 (SELECT id FROM cm_categories WHERE code = 'business' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 28 DAY, NOW(), 0);

-- =============================================
-- 5-1. 코스 태그 데이터 INSERT (cm_course_tags)
-- =============================================
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1), 'Spring'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1) AND tag = 'Spring');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1), 'Java'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1) AND tag = 'Java');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1), 'Backend'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1) AND tag = 'Backend');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1), 'React'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1) AND tag = 'React');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1), 'TypeScript'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1) AND tag = 'TypeScript');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1), 'Frontend'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1) AND tag = 'Frontend');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1), 'Java'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1) AND tag = 'Java');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1), 'OOP'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1) AND tag = 'OOP');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1), 'Backend'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1) AND tag = 'Backend');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1), 'Kubernetes'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1) AND tag = 'Kubernetes');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1), 'DevOps'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1) AND tag = 'DevOps');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1), 'Cloud'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1) AND tag = 'Cloud');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), 'AWS'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1) AND tag = 'AWS');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), 'Cloud'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1) AND tag = 'Cloud');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), 'Infrastructure'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1) AND tag = 'Infrastructure');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1), 'SQL'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1) AND tag = 'SQL');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1), 'Database'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1) AND tag = 'Database');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1), 'Data'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1) AND tag = 'Data');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1), 'Python'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1) AND tag = 'Python');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1), 'Data'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1) AND tag = 'Data');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1), 'Pandas'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1) AND tag = 'Pandas');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1), 'AI'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1) AND tag = 'AI');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1), 'ChatGPT'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1) AND tag = 'ChatGPT');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1), 'Productivity'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'ChatGPT 활용법' AND tenant_id = 1) AND tag = 'Productivity');

INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1), 'DevOps'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1) AND tag = 'DevOps');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1), 'CI/CD'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1) AND tag = 'CI/CD');
INSERT INTO cm_course_tags (course_id, tag)
SELECT (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1), 'Docker'
WHERE NOT EXISTS (SELECT 1 FROM cm_course_tags WHERE course_id = (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1) AND tag = 'Docker');

-- =============================================
-- 5-2. 코스 아이템 데이터 INSERT (cm_course_items) - 커리큘럼 구조
-- =============================================
-- Spring Boot 기초 코스 커리큘럼
INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1), NULL, NULL, '1장. Spring Boot 시작하기', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '1장. Spring Boot 시작하기' AND tenant_id = 1);

INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1), NULL, NULL, '2장. REST API 개발', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '2장. REST API 개발' AND tenant_id = 1);

-- React & TypeScript 코스 커리큘럼
INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1), NULL, NULL, '1장. React 기초', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '1장. React 기초' AND tenant_id = 1);

INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1), NULL, NULL, '2장. TypeScript 심화', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '2장. TypeScript 심화' AND tenant_id = 1);

-- Java 프로그래밍 코스 커리큘럼
INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1), NULL, NULL, '1장. Java 기본 문법', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '1장. Java 기본 문법' AND tenant_id = 1);

INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1), NULL, NULL, '2장. 객체지향 프로그래밍', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '2장. 객체지향 프로그래밍' AND tenant_id = 1);

-- AWS 클라우드 아키텍처 코스 커리큘럼
INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), NULL, NULL, '1장. AWS 기초', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '1장. AWS 기초' AND tenant_id = 1);

-- SQL 완전 정복 코스 커리큘럼
INSERT INTO cm_course_items (tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1), NULL, NULL, '1장. SQL 기초', NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_course_items WHERE item_name = '1장. SQL 기초' AND tenant_id = 1);

-- =============================================
-- 6. 스냅샷 데이터 INSERT (코스 연결)
-- =============================================
INSERT INTO cm_snapshots (tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초' AND tenant_id = 1),
 'Spring Boot 기초 v1', 'Spring Boot 프레임워크의 기본 개념과 실습', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 25 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'React & TypeScript' AND tenant_id = 1),
 'React & TypeScript v1', 'React와 TypeScript를 활용한 프론트엔드 개발', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 20 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍' AND tenant_id = 1),
 'Java 프로그래밍 v1', '자바 언어의 핵심 개념부터 고급 기능까지', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 30 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1),
 'AWS 클라우드 아키텍처 v1', 'AWS 서비스를 활용한 클라우드 인프라 설계', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 23 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영' AND tenant_id = 1),
 'Kubernetes 운영 v1', '쿠버네티스 클러스터 운영 및 모니터링', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 35 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1),
 'SQL 완전 정복 v1', '관계형 데이터베이스와 SQL 쿼리 작성', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 15 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'Python 데이터 분석' AND tenant_id = 1),
 'Python 데이터 분석 v1', 'Pandas, NumPy를 활용한 데이터 분석', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 10 DAY, NOW()),
(1, (SELECT id FROM cm_courses WHERE title = 'DevOps 엔지니어링' AND tenant_id = 1),
 'DevOps 엔지니어링 v1', 'CI/CD 파이프라인 구축과 컨테이너 오케스트레이션', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 13 DAY, NOW());

-- =============================================
-- 7. 콘텐츠 데이터 INSERT
-- =============================================
INSERT INTO content (tenant_id, status, created_by, current_version, original_file_name, content_type, duration, file_path, thumbnail_path, created_at, updated_at, version) VALUES
-- Spring Boot 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'Spring Boot 소개', 'VIDEO', 1800, '/videos/spring-boot-intro.mp4', '/thumbnails/spring-boot-intro.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, '프로젝트 생성하기', 'VIDEO', 2400, '/videos/spring-boot-setup.mp4', '/thumbnails/spring-boot-setup.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'REST API 구현', 'VIDEO', 3600, '/videos/spring-boot-rest.mp4', '/thumbnails/spring-boot-rest.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'JPA와 데이터베이스', 'VIDEO', 4200, '/videos/spring-boot-jpa.mp4', '/thumbnails/spring-boot-jpa.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
-- AWS 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@default.com'), 1, 'AWS 클라우드 개요', 'VIDEO', 2700, '/videos/aws-overview.mp4', '/thumbnails/aws-overview.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@default.com'), 1, 'EC2 인스턴스 생성', 'VIDEO', 3000, '/videos/aws-ec2.mp4', '/thumbnails/aws-ec2.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@default.com'), 1, 'S3 스토리지 활용', 'VIDEO', 2400, '/videos/aws-s3.mp4', '/thumbnails/aws-s3.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
-- React 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'React 컴포넌트 기초', 'VIDEO', 3000, '/videos/react-components.mp4', '/thumbnails/react-components.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'TypeScript 타입 시스템', 'VIDEO', 3600, '/videos/typescript-types.mp4', '/thumbnails/typescript-types.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'creator@default.com'), 1, 'React Hooks 심화', 'VIDEO', 4500, '/videos/react-hooks.mp4', '/thumbnails/react-hooks.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
-- Java 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@default.com'), 1, 'Java 기본 문법', 'VIDEO', 3600, '/videos/java-basics.mp4', '/thumbnails/java-basics.jpg', NOW() - INTERVAL 35 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@default.com'), 1, '객체지향 프로그래밍', 'VIDEO', 5400, '/videos/java-oop.mp4', '/thumbnails/java-oop.jpg', NOW() - INTERVAL 35 DAY, NOW(), 0),
-- Kubernetes 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@default.com'), 1, 'Kubernetes 아키텍처', 'VIDEO', 4200, '/videos/k8s-architecture.mp4', '/thumbnails/k8s-architecture.jpg', NOW() - INTERVAL 40 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@default.com'), 1, 'Pod와 Deployment', 'VIDEO', 3600, '/videos/k8s-pods.mp4', '/thumbnails/k8s-pods.jpg', NOW() - INTERVAL 40 DAY, NOW(), 0),
-- SQL 강의 영상
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@default.com'), 1, 'SELECT 문 기초', 'VIDEO', 1800, '/videos/sql-select.mp4', '/thumbnails/sql-select.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@default.com'), 1, 'JOIN과 서브쿼리', 'VIDEO', 2700, '/videos/sql-join.mp4', '/thumbnails/sql-join.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0);

-- =============================================
-- 8. 스냅샷 학습 객체 데이터 INSERT
-- =============================================
INSERT INTO cm_snapshot_los (tenant_id, content_id, display_name, duration, thumbnail_url, is_customized, created_at, updated_at, version) VALUES
-- Spring Boot 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'Spring Boot 소개' AND tenant_id = 1), 'Spring Boot 소개', 1800, '/thumbnails/spring-boot-intro.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = '프로젝트 생성하기' AND tenant_id = 1), '프로젝트 생성하기', 2400, '/thumbnails/spring-boot-setup.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'REST API 구현' AND tenant_id = 1), 'REST API 구현', 3600, '/thumbnails/spring-boot-rest.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'JPA와 데이터베이스' AND tenant_id = 1), 'JPA와 데이터베이스', 4200, '/thumbnails/spring-boot-jpa.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
-- AWS 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'AWS 클라우드 개요' AND tenant_id = 1), 'AWS 클라우드 개요', 2700, '/thumbnails/aws-overview.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'EC2 인스턴스 생성' AND tenant_id = 1), 'EC2 인스턴스 생성', 3000, '/thumbnails/aws-ec2.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'S3 스토리지 활용' AND tenant_id = 1), 'S3 스토리지 활용', 2400, '/thumbnails/aws-s3.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
-- React 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'React 컴포넌트 기초' AND tenant_id = 1), 'React 컴포넌트 기초', 3000, '/thumbnails/react-components.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'TypeScript 타입 시스템' AND tenant_id = 1), 'TypeScript 타입 시스템', 3600, '/thumbnails/typescript-types.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'React Hooks 심화' AND tenant_id = 1), 'React Hooks 심화', 4500, '/thumbnails/react-hooks.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
-- Java 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'Java 기본 문법' AND tenant_id = 1), 'Java 기본 문법', 3600, '/thumbnails/java-basics.jpg', false, NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = '객체지향 프로그래밍' AND tenant_id = 1), '객체지향 프로그래밍', 5400, '/thumbnails/java-oop.jpg', false, NOW() - INTERVAL 30 DAY, NOW(), 0),
-- Kubernetes 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'Kubernetes 아키텍처' AND tenant_id = 1), 'Kubernetes 아키텍처', 4200, '/thumbnails/k8s-architecture.jpg', false, NOW() - INTERVAL 35 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'Pod와 Deployment' AND tenant_id = 1), 'Pod와 Deployment', 3600, '/thumbnails/k8s-pods.jpg', false, NOW() - INTERVAL 35 DAY, NOW(), 0),
-- SQL 스냅샷 LO
(1, (SELECT id FROM content WHERE original_file_name = 'SELECT 문 기초' AND tenant_id = 1), 'SELECT 문 기초', 1800, '/thumbnails/sql-select.jpg', false, NOW() - INTERVAL 15 DAY, NOW(), 0),
(1, (SELECT id FROM content WHERE original_file_name = 'JOIN과 서브쿼리' AND tenant_id = 1), 'JOIN과 서브쿼리', 2700, '/thumbnails/sql-join.jpg', false, NOW() - INTERVAL 15 DAY, NOW(), 0);

-- =============================================
-- 9. 스냅샷 아이템 데이터 INSERT (커리큘럼 구조)
-- =============================================
-- Spring Boot 스냅샷 - 루트 폴더
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1), NULL, '1장. Spring Boot 시작하기', 0, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1), NULL, '2장. REST API 개발', 0, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0);

-- Spring Boot 스냅샷 - 하위 아이템 (1장)
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '1장. Spring Boot 시작하기' AND tenant_id = 1),
 'Spring Boot 소개', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'Spring Boot 소개' AND tenant_id = 1),
 NOW() - INTERVAL 25 DAY, NOW(), 0;

INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '1장. Spring Boot 시작하기' AND tenant_id = 1),
 '프로젝트 생성하기', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = '프로젝트 생성하기' AND tenant_id = 1),
 NOW() - INTERVAL 25 DAY, NOW(), 0;

-- Spring Boot 스냅샷 - 하위 아이템 (2장)
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '2장. REST API 개발' AND tenant_id = 1),
 'REST API 구현', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'REST API 구현' AND tenant_id = 1),
 NOW() - INTERVAL 25 DAY, NOW(), 0;

INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '2장. REST API 개발' AND tenant_id = 1),
 'JPA와 데이터베이스', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'JPA와 데이터베이스' AND tenant_id = 1),
 NOW() - INTERVAL 25 DAY, NOW(), 0;

-- AWS 스냅샷 - 루트 폴더 및 아이템
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'AWS 클라우드 아키텍처 v1' AND tenant_id = 1), NULL, 'AWS 기초', 0, NULL, NULL, NOW() - INTERVAL 23 DAY, NOW(), 0);

INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'AWS 클라우드 아키텍처 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'AWS 기초' AND tenant_id = 1),
 'AWS 클라우드 개요', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'AWS 클라우드 개요' AND tenant_id = 1),
 NOW() - INTERVAL 23 DAY, NOW(), 0;

INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'AWS 클라우드 아키텍처 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'AWS 기초' AND tenant_id = 1),
 'EC2 인스턴스 생성', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'EC2 인스턴스 생성' AND tenant_id = 1),
 NOW() - INTERVAL 23 DAY, NOW(), 0;

INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'AWS 클라우드 아키텍처 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'AWS 기초' AND tenant_id = 1),
 'S3 스토리지 활용', 1, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'S3 스토리지 활용' AND tenant_id = 1),
 NOW() - INTERVAL 23 DAY, NOW(), 0;

-- React 스냅샷 - 루트 아이템 (폴더 없이 직접 배치)
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1), NULL, 'React 컴포넌트 기초', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'React 컴포넌트 기초' AND tenant_id = 1),
 NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1), NULL, 'TypeScript 타입 시스템', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'TypeScript 타입 시스템' AND tenant_id = 1),
 NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1), NULL, 'React Hooks 심화', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'React Hooks 심화' AND tenant_id = 1),
 NOW() - INTERVAL 20 DAY, NOW(), 0);

-- Java 스냅샷 - 루트 아이템
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Java 프로그래밍 v1' AND tenant_id = 1), NULL, 'Java 기본 문법', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'Java 기본 문법' AND tenant_id = 1),
 NOW() - INTERVAL 30 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Java 프로그래밍 v1' AND tenant_id = 1), NULL, '객체지향 프로그래밍', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = '객체지향 프로그래밍' AND tenant_id = 1),
 NOW() - INTERVAL 30 DAY, NOW(), 0);

-- Kubernetes 스냅샷 - 루트 아이템
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Kubernetes 운영 v1' AND tenant_id = 1), NULL, 'Kubernetes 아키텍처', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'Kubernetes 아키텍처' AND tenant_id = 1),
 NOW() - INTERVAL 35 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Kubernetes 운영 v1' AND tenant_id = 1), NULL, 'Pod와 Deployment', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'Pod와 Deployment' AND tenant_id = 1),
 NOW() - INTERVAL 35 DAY, NOW(), 0);

-- SQL 스냅샷 - 루트 아이템
INSERT INTO cm_snapshot_items (tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'SQL 완전 정복 v1' AND tenant_id = 1), NULL, 'SELECT 문 기초', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'SELECT 문 기초' AND tenant_id = 1),
 NOW() - INTERVAL 15 DAY, NOW(), 0),
(1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'SQL 완전 정복 v1' AND tenant_id = 1), NULL, 'JOIN과 서브쿼리', 0, 'VIDEO',
 (SELECT id FROM cm_snapshot_los WHERE display_name = 'JOIN과 서브쿼리' AND tenant_id = 1),
 NOW() - INTERVAL 15 DAY, NOW(), 0);

-- =============================================
-- 9-1. 스냅샷 관계 데이터 INSERT (cm_snapshot_relations) - 학습 순서
-- =============================================
-- Spring Boot 스냅샷의 학습 순서: Spring Boot 소개 -> 프로젝트 생성하기 -> REST API 구현 -> JPA와 데이터베이스
INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 NULL,
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'Spring Boot 소개' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id IS NULL AND to_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'Spring Boot 소개' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'Spring Boot 소개' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '프로젝트 생성하기' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'Spring Boot 소개' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '프로젝트 생성하기' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'REST API 구현' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = '프로젝트 생성하기' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'REST API 구현' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'JPA와 데이터베이스' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'REST API 구현' AND tenant_id = 1 LIMIT 1));

-- React 스냅샷의 학습 순서
INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1),
 NULL,
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'React 컴포넌트 기초' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id IS NULL AND to_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'React 컴포넌트 기초' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'React 컴포넌트 기초' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'TypeScript 타입 시스템' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'React 컴포넌트 기초' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'TypeScript 타입 시스템' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'React Hooks 심화' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'TypeScript 타입 시스템' AND tenant_id = 1 LIMIT 1));

-- Java 스냅샷의 학습 순서
INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Java 프로그래밍 v1' AND tenant_id = 1),
 NULL,
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'Java 기본 문법' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id IS NULL AND to_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'Java 기본 문법' AND tenant_id = 1 LIMIT 1));

INSERT INTO cm_snapshot_relations (tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version)
SELECT 1, (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Java 프로그래밍 v1' AND tenant_id = 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = 'Java 기본 문법' AND tenant_id = 1 LIMIT 1),
 (SELECT id FROM cm_snapshot_items WHERE item_name = '객체지향 프로그래밍' AND tenant_id = 1 LIMIT 1),
 NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM cm_snapshot_relations WHERE tenant_id = 1 AND from_item_id = (SELECT id FROM cm_snapshot_items WHERE item_name = 'Java 기본 문법' AND tenant_id = 1 LIMIT 1));

-- =============================================
-- 10. 프로그램 데이터 INSERT (스냅샷 연결) - cm_programs 테이블 제거로 비활성화
-- =============================================
-- cm_programs 테이블이 제거되어 아래 INSERT 문은 비활성화됨
/*
INSERT INTO cm_programs (tenant_id, title, description, level, type, estimated_hours, snapshot_id, status, created_by, submitted_at, approved_by, approved_at, rejection_reason, rejected_at, created_at, updated_at, version) VALUES
-- APPROVED 상태 (스냅샷 연결됨)
(1, 'Spring Boot 기초 과정', 'Spring Boot 프레임워크의 기본 개념과 실습을 다루는 입문 과정입니다.', 'BEGINNER', 'ONLINE', 20,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Spring Boot 기초 v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 10 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 7 DAY, NULL, NULL, NOW() - INTERVAL 14 DAY, NOW(), 0),
(1, 'AWS 클라우드 아키텍처', 'AWS 서비스를 활용한 클라우드 인프라 설계 및 구축 과정입니다.', 'INTERMEDIATE', 'BLENDED', 40,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'AWS 클라우드 아키텍처 v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 8 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 5 DAY, NULL, NULL, NOW() - INTERVAL 12 DAY, NOW(), 0),
(1, 'React & TypeScript 실전', 'React와 TypeScript를 활용한 프론트엔드 개발 심화 과정입니다.', 'ADVANCED', 'ONLINE', 30,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'React & TypeScript v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'creator@default.com'), NOW() - INTERVAL 6 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 4 DAY, NULL, NULL, NOW() - INTERVAL 10 DAY, NOW(), 0),
(1, 'Java 프로그래밍 마스터', '자바 언어의 핵심 개념부터 고급 기능까지 체계적으로 학습합니다.', 'INTERMEDIATE', 'ONLINE', 45,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Java 프로그래밍 v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 12 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 9 DAY, NULL, NULL, NOW() - INTERVAL 16 DAY, NOW(), 0),
(1, 'Kubernetes 운영 실무', '쿠버네티스 클러스터 운영 및 모니터링 실무 과정입니다.', 'ADVANCED', 'BLENDED', 50,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Kubernetes 운영 v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 15 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 12 DAY, NULL, NULL, NOW() - INTERVAL 20 DAY, NOW(), 0),
(1, 'SQL 완전 정복', '관계형 데이터베이스와 SQL 쿼리 작성의 모든 것을 다룹니다.', 'BEGINNER', 'ONLINE', 15,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'SQL 완전 정복 v1' AND tenant_id = 1),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 5 DAY, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 3 DAY, NULL, NULL, NOW() - INTERVAL 8 DAY, NOW(), 0),
-- PENDING 상태 (스냅샷 연결됨)
(1, 'DevOps 엔지니어링', 'CI/CD 파이프라인 구축과 컨테이너 오케스트레이션을 학습합니다.', 'INTERMEDIATE', 'OFFLINE', 35,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'DevOps 엔지니어링 v1' AND tenant_id = 1),
 'PENDING', (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 3 DAY, NULL, NULL, NULL, NULL, NOW() - INTERVAL 5 DAY, NOW(), 0),
(1, 'Python 데이터 분석', 'Pandas, NumPy를 활용한 데이터 분석 기초 과정입니다.', 'BEGINNER', 'ONLINE', 25,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'Python 데이터 분석 v1' AND tenant_id = 1),
 'PENDING', (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 2 DAY, NULL, NULL, NULL, NULL, NOW() - INTERVAL 4 DAY, NOW(), 0),
-- PENDING 상태 (스냅샷 없음)
(1, 'Node.js 백엔드 개발', 'Node.js와 Express를 활용한 백엔드 서버 개발 과정입니다.', 'INTERMEDIATE', 'ONLINE', 30,
 NULL,
 'PENDING', (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 1 DAY, NULL, NULL, NULL, NULL, NOW() - INTERVAL 3 DAY, NOW(), 0),
-- REJECTED 상태 (스냅샷 없음)
(1, 'AI/ML 입문 과정', '인공지능과 머신러닝의 기초 개념을 학습합니다.', 'BEGINNER', 'ONLINE', 30,
 NULL,
 'REJECTED', (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW() - INTERVAL 7 DAY, NULL, NULL, '학습 목표가 불명확합니다. 구체적인 학습 성과를 명시해주세요.', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 10 DAY, NOW(), 0),
(1, '블록체인 개발', '이더리움 스마트 컨트랙트 개발 과정입니다.', 'ADVANCED', 'OFFLINE', 40,
 NULL,
 'REJECTED', (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW() - INTERVAL 6 DAY, NULL, NULL, '예상 학습 시간 대비 커리큘럼 내용이 부족합니다.', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 9 DAY, NOW(), 0),
-- DRAFT 상태 (스냅샷 없음)
(1, 'Git & GitHub 협업', '버전 관리와 협업 워크플로우를 학습하는 과정입니다.', 'BEGINNER', 'ONLINE', 10,
 NULL,
 'DRAFT', (SELECT id FROM users WHERE email = 'designer1@default.com'), NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW(), 0),
(1, 'MSA 설계 패턴', '마이크로서비스 아키텍처 설계 원칙과 패턴을 다룹니다.', 'ADVANCED', 'BLENDED', 35,
 NULL,
 'DRAFT', (SELECT id FROM users WHERE email = 'designer2@default.com'), NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL 2 HOUR, NOW(), 0);
*/

-- =============================================
-- 11. 차수 데이터 INSERT (Current Date: 2026-01-06)
-- =============================================
INSERT INTO course_times (
    tenant_id, course_id, title, delivery_type, status,
    enroll_start_date, enroll_end_date, class_start_date, class_end_date,
    capacity, max_waiting_count, current_enrollment, enrollment_method,
    min_progress_for_completion, price, is_free, allow_late_enrollment,
    created_by, created_at, updated_at, version
) VALUES

-- 1. DRAFT (작성 중)
-- 로직: 모집 시작일이 미래(2026년 2월)여야 함
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1 LIMIT 1),
 'Spring Boot 기초 1차', 'ONLINE', 'DRAFT',
 '2026-02-01', '2026-02-15', '2026-02-20', '2026-03-20',
 30, 5, 0, 'FIRST_COME', 80, 0.00, true, false, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1 LIMIT 1),
 'AWS 클라우드 아키텍처 1차', 'BLENDED', 'DRAFT',
 '2026-03-01', '2026-03-15', '2026-03-20', '2026-05-20',
 20, 3, 0, 'APPROVAL', 70, 150000.00, false, false, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

-- 2. RECRUITING (모집 중)
-- 로직: 현재(1/6)가 enroll_start와 enroll_end 사이에 있어야 함
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1 LIMIT 1),
 'Spring Boot 기초 2차', 'ONLINE', 'RECRUITING',
 '2026-01-01', '2026-01-15', '2026-01-20', '2026-02-20',
 25, 5, 12, 'FIRST_COME', 80, 0.00, true, true, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'React & TypeScript 실전' AND tenant_id = 1 LIMIT 1),
 'React & TypeScript 실전 1차', 'LIVE', 'RECRUITING',
 '2025-12-20', '2026-01-10', '2026-01-15', '2026-02-28',
 40, 10, 28, 'FIRST_COME', 75, 200000.00, false, true, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1 LIMIT 1),
 'AWS 클라우드 아키텍처 2차', 'OFFLINE', 'RECRUITING',
 '2026-01-02', '2026-01-20', '2026-02-01', '2026-03-01',
 15, 0, 15, 'APPROVAL', 70, 180000.00, false, false, (SELECT id FROM users WHERE email = 'operator2@default.com'), NOW(), NOW(), 0),

-- 3. ONGOING (진행 중)
-- 로직: 현재(1/6)가 class_start와 class_end 사이에 있어야 함 (모집은 이미 끝남)
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1 LIMIT 1),
 'Spring Boot 기초 3차', 'ONLINE', 'ONGOING',
 '2025-11-01', '2025-11-30', '2025-12-01', '2026-01-31',
 30, 5, 30, 'FIRST_COME', 80, 0.00, true, true, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍 마스터' AND tenant_id = 1 LIMIT 1),
 'Java 프로그래밍 마스터 1차', 'ONLINE', 'ONGOING',
 '2025-11-15', '2025-12-15', '2025-12-20', '2026-02-20',
 50, 10, 45, 'FIRST_COME', 85, 100000.00, false, false, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'Kubernetes 운영 실무' AND tenant_id = 1 LIMIT 1),
 'Kubernetes 운영 실무 1차', 'BLENDED', 'ONGOING',
 '2025-10-01', '2025-10-31', '2025-11-01', '2026-01-10',
 20, 5, 18, 'APPROVAL', 90, 300000.00, false, true, (SELECT id FROM users WHERE email = 'operator2@default.com'), NOW(), NOW(), 0),

-- 4. CLOSED (종료)
-- 로직: class_end가 과거(2025년 말)여야 함
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1 LIMIT 1),
 'Spring Boot 기초 4차', 'ONLINE', 'CLOSED',
 '2025-09-01', '2025-09-15', '2025-09-20', '2025-10-20',
 30, 5, 28, 'FIRST_COME', 80, 0.00, true, false, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1 LIMIT 1),
 'SQL 완전 정복 1차', 'ONLINE', 'CLOSED',
 '2025-11-01', '2025-11-15', '2025-11-20', '2025-12-31',
 100, 20, 87, 'FIRST_COME', 70, 50000.00, false, true, (SELECT id FROM users WHERE email = 'operator2@default.com'), NOW(), NOW(), 0),

-- 5. ARCHIVED (보관)
-- 로직: class_end가 먼 과거(2025년 상반기)여야 함
(1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1 LIMIT 1),
 'Spring Boot 기초 0차 (파일럿)', 'ONLINE', 'ARCHIVED',
 '2025-01-01', '2025-01-15', '2025-01-20', '2025-02-20',
 10, 0, 10, 'INVITE_ONLY', 80, 0.00, true, false, (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW(), NOW(), 0),

(1, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍 마스터' AND tenant_id = 1 LIMIT 1),
 'Java 프로그래밍 마스터 0차', 'OFFLINE', 'ARCHIVED',
 '2025-03-01', '2025-03-15', '2025-03-20', '2025-05-20',
 20, 5, 18, 'APPROVAL', 85, 80000.00, false, false, (SELECT id FROM users WHERE email = 'operator2@default.com'), NOW(), NOW(), 0);
-- =============================================
-- 12. 강사 배정 데이터 INSERT (코스 설계자 자동 배정)
-- =============================================
INSERT INTO iis_instructor_assignments (tenant_id, user_key, time_key, assigned_at, role, status, assigned_by, created_at, updated_at, version) VALUES
-- Spring Boot 기초 1차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- Spring Boot 기초 2차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 2차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- React & TypeScript 실전 1차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- AWS 클라우드 아키텍처 1차 (설계자: designer)
(1, (SELECT id FROM users WHERE email = 'designer1@default.com'),
 (SELECT id FROM course_times WHERE title = 'AWS 클라우드 아키텍처 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW(), NOW(), 0),
-- AWS 클라우드 아키텍처 2차 (설계자: designer)
(1, (SELECT id FROM users WHERE email = 'designer1@default.com'),
 (SELECT id FROM course_times WHERE title = 'AWS 클라우드 아키텍처 2차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW(), NOW(), 0),
-- Spring Boot 기초 3차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- Java 프로그래밍 마스터 1차 (설계자: designer)
(1, (SELECT id FROM users WHERE email = 'designer1@default.com'),
 (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW(), NOW(), 0),
-- Kubernetes 운영 실무 1차 (설계자: designer2)
(1, (SELECT id FROM users WHERE email = 'designer2@default.com'),
 (SELECT id FROM course_times WHERE title = 'Kubernetes 운영 실무 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW(), NOW(), 0),
-- Spring Boot 기초 4차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 4차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- SQL 완전 정복 1차 (설계자: designer2)
(1, (SELECT id FROM users WHERE email = 'designer2@default.com'),
 (SELECT id FROM course_times WHERE title = 'SQL 완전 정복 1차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer2@default.com'), NOW(), NOW(), 0),
-- Spring Boot 기초 0차 (설계자: creator)
(1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 0차 (파일럿)' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'creator@default.com'), NOW(), NOW(), 0),
-- Java 프로그래밍 마스터 0차 (설계자: designer)
(1, (SELECT id FROM users WHERE email = 'designer1@default.com'),
 (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 0차' AND tenant_id = 1),
 NOW(), 'MAIN', 'ACTIVE',
 (SELECT id FROM users WHERE email = 'designer1@default.com'), NOW(), NOW(), 0);

-- =============================================
-- 13. 사용자 코스 역할 데이터 INSERT (USER → DESIGNER 흐름)
-- =============================================
-- creator@default.com: USER + DESIGNER (프로그램 승인받음)
-- DESIGNER 역할 (courseId = null, 테넌트 레벨)
INSERT INTO user_course_roles (tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at)
SELECT 1, (SELECT id FROM users WHERE email = 'creator@default.com'), NULL, 'DESIGNER', NULL, NOW(), NOW();
-- DESIGNER 역할 (courseId = programId, 프로그램별 설계자)
INSERT INTO user_course_roles (tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at)
SELECT 1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1), 'DESIGNER', 70, NOW(), NOW();
INSERT INTO user_course_roles (tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at)
SELECT 1, (SELECT id FROM users WHERE email = 'creator@default.com'),
 (SELECT id FROM cm_courses WHERE title = 'React & TypeScript 실전' AND tenant_id = 1), 'DESIGNER', 70, NOW(), NOW();

-- designer3@default.com: USER + DESIGNER (프로그램 아직 미승인)
-- DESIGNER 역할만 (courseId = null, 테넌트 레벨)
INSERT INTO user_course_roles (tenant_id, user_id, course_id, role, revenue_share_percent, created_at, updated_at)
SELECT 1, (SELECT id FROM users WHERE email = 'designer3@default.com'), NULL, 'DESIGNER', NULL, NOW(), NOW();

-- =============================================
-- 14. 수강 데이터 INSERT (다양한 수강 상태 테스트)
-- =============================================
INSERT INTO sis_enrollments (tenant_id, user_id, course_time_id, enrolled_at, type, status, progress_percent, score, completed_at, enrolled_by, created_at, updated_at, version) VALUES
-- === Spring Boot 기초 3차 (ONGOING) 수강생 ===
-- student1: 수강 중 (진도율 60%)
(1, (SELECT id FROM users WHERE email = 'user1@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1),
 NOW() - INTERVAL 10 DAY, 'VOLUNTARY', 'ENROLLED', 60, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user1@default.com'), NOW() - INTERVAL 10 DAY, NOW(), 0),
-- student2: 수강 중 (진도율 30%)
(1, (SELECT id FROM users WHERE email = 'user2@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1),
 NOW() - INTERVAL 8 DAY, 'VOLUNTARY', 'ENROLLED', 30, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user2@default.com'), NOW() - INTERVAL 8 DAY, NOW(), 0),
-- user1: 수강 중 (진도율 85%)
(1, (SELECT id FROM users WHERE email = 'user7@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1),
 NOW() - INTERVAL 12 DAY, 'VOLUNTARY', 'ENROLLED', 85, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user7@default.com'), NOW() - INTERVAL 12 DAY, NOW(), 0),

-- === Spring Boot 기초 4차 (CLOSED) 수강생 ===
-- student3: 수료 완료 (95점)
(1, (SELECT id FROM users WHERE email = 'user3@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 4차' AND tenant_id = 1),
 NOW() - INTERVAL 60 DAY, 'VOLUNTARY', 'COMPLETED', 100, 95, NOW() - INTERVAL 5 DAY,
 (SELECT id FROM users WHERE email = 'user3@default.com'), NOW() - INTERVAL 60 DAY, NOW(), 0),
-- student4: 수료 완료 (88점)
(1, (SELECT id FROM users WHERE email = 'user4@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 4차' AND tenant_id = 1),
 NOW() - INTERVAL 55 DAY, 'VOLUNTARY', 'COMPLETED', 100, 88, NOW() - INTERVAL 7 DAY,
 (SELECT id FROM users WHERE email = 'user4@default.com'), NOW() - INTERVAL 55 DAY, NOW(), 0),
-- user2: 중도 포기 (진도율 40%에서 취소)
(1, (SELECT id FROM users WHERE email = 'user8@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 4차' AND tenant_id = 1),
 NOW() - INTERVAL 58 DAY, 'VOLUNTARY', 'DROPPED', 40, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user8@default.com'), NOW() - INTERVAL 58 DAY, NOW(), 0),
-- user3: 미이수 (기간 내 미완료)
(1, (SELECT id FROM users WHERE email = 'user9@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 4차' AND tenant_id = 1),
 NOW() - INTERVAL 50 DAY, 'VOLUNTARY', 'FAILED', 65, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user9@default.com'), NOW() - INTERVAL 50 DAY, NOW(), 0),

-- === React & TypeScript 실전 1차 (RECRUITING) 수강생 ===
-- student5: 수강 중 (진도율 15%)
(1, (SELECT id FROM users WHERE email = 'user5@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW() - INTERVAL 5 DAY, 'VOLUNTARY', 'ENROLLED', 15, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user5@default.com'), NOW() - INTERVAL 5 DAY, NOW(), 0),
-- student1: 수강 중 (진도율 25%) - student1은 Spring Boot와 React 동시 수강
(1, (SELECT id FROM users WHERE email = 'user1@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW() - INTERVAL 7 DAY, 'VOLUNTARY', 'ENROLLED', 25, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user1@default.com'), NOW() - INTERVAL 7 DAY, NOW(), 0),

-- === Java 프로그래밍 마스터 1차 (ONGOING) 수강생 ===
-- student6: 수강 중 (진도율 50%)
(1, (SELECT id FROM users WHERE email = 'user6@default.com'),
 (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1),
 NOW() - INTERVAL 15 DAY, 'VOLUNTARY', 'ENROLLED', 50, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user6@default.com'), NOW() - INTERVAL 15 DAY, NOW(), 0),
-- user4: 필수 교육 강제 배정 (진도율 20%)
(1, (SELECT id FROM users WHERE email = 'user10@default.com'),
 (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1),
 NOW() - INTERVAL 10 DAY, 'MANDATORY', 'ENROLLED', 20, NULL, NULL,
 (SELECT id FROM users WHERE email = 'operator1@default.com'), NOW() - INTERVAL 10 DAY, NOW(), 0),

-- === Kubernetes 운영 실무 1차 (ONGOING) 수강생 ===
-- student2: 수강 중 (진도율 70%) - student2는 Spring Boot와 Kubernetes 동시 수강
(1, (SELECT id FROM users WHERE email = 'user2@default.com'),
 (SELECT id FROM course_times WHERE title = 'Kubernetes 운영 실무 1차' AND tenant_id = 1),
 NOW() - INTERVAL 20 DAY, 'VOLUNTARY', 'ENROLLED', 70, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user2@default.com'), NOW() - INTERVAL 20 DAY, NOW(), 0),

-- === SQL 완전 정복 1차 (CLOSED) 수강생 ===
-- student3: 수료 완료 (100점) - student3은 Spring Boot와 SQL 모두 수료
(1, (SELECT id FROM users WHERE email = 'user3@default.com'),
 (SELECT id FROM course_times WHERE title = 'SQL 완전 정복 1차' AND tenant_id = 1),
 NOW() - INTERVAL 40 DAY, 'VOLUNTARY', 'COMPLETED', 100, 100, NOW() - INTERVAL 10 DAY,
 (SELECT id FROM users WHERE email = 'user3@default.com'), NOW() - INTERVAL 40 DAY, NOW(), 0),
-- student5: 중도 포기
(1, (SELECT id FROM users WHERE email = 'user5@default.com'),
 (SELECT id FROM course_times WHERE title = 'SQL 완전 정복 1차' AND tenant_id = 1),
 NOW() - INTERVAL 35 DAY, 'VOLUNTARY', 'DROPPED', 25, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user5@default.com'), NOW() - INTERVAL 35 DAY, NOW(), 0);

-- =============================================
-- [필수] 외래 키 검사 다시 활성화 (데이터 무결성 보호)
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 테스트용 Roadmap 데이터 (Safe vs Destructive Update 테스트용)
-- ============================================

-- Roadmap 1: DRAFT 상태 (수강생 0명)
-- → 모든 변경 허용 (프로그램 삭제, 순서 변경 등)
INSERT INTO roadmaps (id, tenant_id, title, description, author_id, status, enrolled_students, created_at, updated_at)
SELECT 1, 1, '[테스트] 백엔드 개발자 성장 로드맵 (DRAFT)',
       '백엔드 개발자로 성장하기 위한 학습 경로입니다. 현재 작성 중입니다.',
       (SELECT id FROM users WHERE email = 'creator@default.com'), 'DRAFT', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roadmaps WHERE id = 1);

-- Roadmap 1에 프로그램 연결 (Spring Boot 기초 과정, AWS 클라우드 아키텍처)
-- [DISABLED] Program 엔티티 제거로 인해 주석 처리
/*
INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1), 0
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 1 AND program_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 1, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), 1
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 1 AND program_id = (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1));
*/

-- Roadmap 2: PUBLISHED 상태 (수강생 0명)
-- → 프로그램 삭제, 순서 변경 허용
INSERT INTO roadmaps (id, tenant_id, title, description, author_id, status, enrolled_students, created_at, updated_at)
SELECT 2, 1, '[테스트] 프론트엔드 마스터 로드맵 (공개 - 수강생 없음)',
       '프론트엔드 개발자를 위한 종합 학습 로드맵입니다. 현재 수강생이 없어 자유롭게 수정 가능합니다.',
       (SELECT id FROM users WHERE email = 'creator@default.com'), 'PUBLISHED', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roadmaps WHERE id = 2);

-- Roadmap 2에 프로그램 연결 (Spring Boot 기초 과정, Java 프로그래밍 마스터)
-- [DISABLED] Program 엔티티 제거로 인해 주석 처리
/*
INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 2, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1), 0
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 2 AND program_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 2, (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍 마스터' AND tenant_id = 1), 1
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 2 AND program_id = (SELECT id FROM cm_courses WHERE title = 'Java 프로그래밍 마스터' AND tenant_id = 1));
*/

-- Roadmap 3: PUBLISHED 상태 (수강생 3명)
-- → 프로그램 삭제, 순서 변경 차단 (핵심 테스트 대상)
-- → 메타데이터 변경, 프로그램 추가는 허용
INSERT INTO roadmaps (id, tenant_id, title, description, author_id, status, enrolled_students, created_at, updated_at)
SELECT 3, 1, '[테스트] 풀스택 개발자 로드맵 (공개 - 수강생 있음)',
       '풀스택 개발자를 위한 완벽한 학습 경로입니다. 현재 3명의 수강생이 학습 중이므로 프로그램 삭제 및 순서 변경이 제한됩니다.',
       (SELECT id FROM users WHERE email = 'creator@default.com'), 'PUBLISHED', 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roadmaps WHERE id = 3);

-- Roadmap 3에 프로그램 연결 (Spring Boot 기초 과정, AWS 클라우드 아키텍처, SQL 완전 정복)
-- [DISABLED] Program 엔티티 제거로 인해 주석 처리
/*
INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 3, (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1), 0
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 3 AND program_id = (SELECT id FROM cm_courses WHERE title = 'Spring Boot 기초 과정' AND tenant_id = 1));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 3, (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1), 1
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 3 AND program_id = (SELECT id FROM cm_courses WHERE title = 'AWS 클라우드 아키텍처' AND tenant_id = 1));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT 3, (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1), 2
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = 3 AND program_id = (SELECT id FROM cm_courses WHERE title = 'SQL 완전 정복' AND tenant_id = 1));
*/

-- =============================================
-- 15. 테넌트 2 (A사 교육센터) 카테고리 데이터
-- =============================================
INSERT INTO cm_categories (tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(2, '영업 교육', 'sales', 1, true, NOW(), NOW()),
(2, '리더십', 'leadership', 2, true, NOW(), NOW()),
(2, '직무 역량', 'competency', 3, true, NOW(), NOW()),
(2, '신입 교육', 'onboarding', 4, true, NOW(), NOW()),
(2, '컴플라이언스', 'compliance', 5, true, NOW(), NOW());

-- =============================================
-- 16. 테넌트 3 (B사 아카데미) 카테고리 데이터
-- =============================================
INSERT INTO cm_categories (tenant_id, name, code, sort_order, active, created_at, updated_at) VALUES
(3, '스타트업 기초', 'startup', 1, true, NOW(), NOW()),
(3, '마케팅', 'marketing', 2, true, NOW(), NOW()),
(3, '프로덕트', 'product', 3, true, NOW(), NOW()),
(3, '그로스해킹', 'growth', 4, true, NOW(), NOW()),
(3, '투자유치', 'funding', 5, true, NOW(), NOW());

-- =============================================
-- 17. 테넌트 2 코스 데이터 (20개)
-- =============================================
INSERT INTO cm_courses (tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version) VALUES
-- 영업 교육
(2, '영업 기초 이론', 'B2B 영업의 기본 개념과 프로세스', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'sales' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 30 DAY, NOW(), 0),
(2, '협상 스킬 마스터', '효과적인 협상 전략과 기법', 'INTERMEDIATE', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'sales' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 28 DAY, NOW(), 0),
(2, '고객 관계 관리', 'CRM 전략과 고객 유지 방법', 'INTERMEDIATE', 'BLENDED', 20,
 (SELECT id FROM cm_categories WHERE code = 'sales' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 25 DAY, NOW(), 0),
(2, '세일즈 프레젠테이션', '설득력 있는 영업 발표 기법', 'BEGINNER', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'sales' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 20 DAY, NOW(), 0),
-- 리더십
(2, '팀 리더십 기초', '효과적인 팀 운영과 리더십 스킬', 'BEGINNER', 'ONLINE', 12,
 (SELECT id FROM cm_categories WHERE code = 'leadership' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 35 DAY, NOW(), 0),
(2, '조직 변화 관리', '변화 주도와 팀 동기부여 전략', 'ADVANCED', 'BLENDED', 25,
 (SELECT id FROM cm_categories WHERE code = 'leadership' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 32 DAY, NOW(), 0),
(2, '임원 리더십 과정', '경영진을 위한 전략적 리더십', 'ADVANCED', 'OFFLINE', 30,
 (SELECT id FROM cm_categories WHERE code = 'leadership' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 40 DAY, NOW(), 0),
(2, '코칭 스킬', '팀원 성장을 위한 코칭 기법', 'INTERMEDIATE', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'leadership' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 22 DAY, NOW(), 0),
-- 직무 역량
(2, '비즈니스 커뮤니케이션', '효과적인 업무 소통 전략', 'BEGINNER', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'competency' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 18 DAY, NOW(), 0),
(2, '문제 해결 역량', '체계적 문제 분석과 해결 방법론', 'INTERMEDIATE', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'competency' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 15 DAY, NOW(), 0),
(2, '시간 관리 기술', '업무 효율성 극대화 전략', 'BEGINNER', 'ONLINE', 6,
 (SELECT id FROM cm_categories WHERE code = 'competency' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 12 DAY, NOW(), 0),
(2, '프로젝트 관리', 'PM 역량 강화를 위한 실무 과정', 'INTERMEDIATE', 'BLENDED', 20,
 (SELECT id FROM cm_categories WHERE code = 'competency' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 10 DAY, NOW(), 0),
-- 신입 교육
(2, '신입사원 OJT', '회사 적응을 위한 기본 교육', 'BEGINNER', 'BLENDED', 40,
 (SELECT id FROM cm_categories WHERE code = 'onboarding' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 45 DAY, NOW(), 0),
(2, '직장 예절 교육', '비즈니스 매너와 에티켓', 'BEGINNER', 'ONLINE', 5,
 (SELECT id FROM cm_categories WHERE code = 'onboarding' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 42 DAY, NOW(), 0),
(2, '업무 도구 활용', 'MS Office 및 협업 도구 마스터', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'onboarding' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 38 DAY, NOW(), 0),
(2, '조직 문화 이해', 'A사의 핵심 가치와 문화', 'BEGINNER', 'ONLINE', 4,
 (SELECT id FROM cm_categories WHERE code = 'onboarding' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 35 DAY, NOW(), 0),
-- 컴플라이언스
(2, '정보보안 기초', '정보 보안 인식 제고 교육', 'BEGINNER', 'ONLINE', 3,
 (SELECT id FROM cm_categories WHERE code = 'compliance' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 50 DAY, NOW(), 0),
(2, '개인정보보호법', '개인정보 처리 및 보호 의무', 'BEGINNER', 'ONLINE', 4,
 (SELECT id FROM cm_categories WHERE code = 'compliance' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 48 DAY, NOW(), 0),
(2, '직장 내 괴롭힘 예방', '건강한 직장 문화 형성', 'BEGINNER', 'ONLINE', 2,
 (SELECT id FROM cm_categories WHERE code = 'compliance' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 46 DAY, NOW(), 0),
(2, '윤리 경영', '기업 윤리와 준법 경영', 'INTERMEDIATE', 'ONLINE', 5,
 (SELECT id FROM cm_categories WHERE code = 'compliance' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 44 DAY, NOW(), 0);

-- =============================================
-- 18. 테넌트 3 코스 데이터 (20개)
-- =============================================
INSERT INTO cm_courses (tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version) VALUES
-- 스타트업 기초
(3, '스타트업 101', '스타트업 창업의 기본 개념', 'BEGINNER', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'startup' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 30 DAY, NOW(), 0),
(3, '린 스타트업 방법론', '빠른 실험과 검증 기반 성장', 'INTERMEDIATE', 'ONLINE', 12,
 (SELECT id FROM cm_categories WHERE code = 'startup' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 28 DAY, NOW(), 0),
(3, '비즈니스 모델 캔버스', '효과적인 BM 설계 방법', 'BEGINNER', 'BLENDED', 10,
 (SELECT id FROM cm_categories WHERE code = 'startup' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 25 DAY, NOW(), 0),
(3, '팀 빌딩 전략', '스타트업 핵심 인재 확보', 'INTERMEDIATE', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'startup' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 22 DAY, NOW(), 0),
-- 마케팅
(3, '디지털 마케팅 기초', '온라인 마케팅 전략 입문', 'BEGINNER', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'marketing' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 35 DAY, NOW(), 0),
(3, '콘텐츠 마케팅', '효과적인 콘텐츠 제작과 배포', 'INTERMEDIATE', 'ONLINE', 20,
 (SELECT id FROM cm_categories WHERE code = 'marketing' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 32 DAY, NOW(), 0),
(3, 'SNS 마케팅 전략', '소셜 미디어 채널 운영', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'marketing' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 30 DAY, NOW(), 0),
(3, '퍼포먼스 마케팅', '데이터 기반 광고 최적화', 'ADVANCED', 'BLENDED', 25,
 (SELECT id FROM cm_categories WHERE code = 'marketing' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 28 DAY, NOW(), 0),
-- 프로덕트
(3, '프로덕트 매니지먼트', 'PM 역할과 핵심 역량', 'INTERMEDIATE', 'ONLINE', 20,
 (SELECT id FROM cm_categories WHERE code = 'product' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 40 DAY, NOW(), 0),
(3, 'UX/UI 디자인 기초', '사용자 경험 설계 입문', 'BEGINNER', 'ONLINE', 15,
 (SELECT id FROM cm_categories WHERE code = 'product' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 38 DAY, NOW(), 0),
(3, '애자일 스크럼', '애자일 방법론 실무 적용', 'INTERMEDIATE', 'BLENDED', 12,
 (SELECT id FROM cm_categories WHERE code = 'product' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 35 DAY, NOW(), 0),
(3, '사용자 리서치', '고객 니즈 파악과 인사이트', 'INTERMEDIATE', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'product' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 32 DAY, NOW(), 0),
-- 그로스해킹
(3, '그로스해킹 기초', '데이터 기반 성장 전략', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'growth' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 20 DAY, NOW(), 0),
(3, 'A/B 테스트 실전', '가설 검증과 실험 설계', 'INTERMEDIATE', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'growth' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 18 DAY, NOW(), 0),
(3, '리텐션 전략', '사용자 유지와 재방문 유도', 'ADVANCED', 'ONLINE', 12,
 (SELECT id FROM cm_categories WHERE code = 'growth' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 15 DAY, NOW(), 0),
(3, '바이럴 마케팅', '입소문 효과 극대화 전략', 'INTERMEDIATE', 'BLENDED', 10,
 (SELECT id FROM cm_categories WHERE code = 'growth' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 12 DAY, NOW(), 0),
-- 투자유치
(3, 'IR 피칭 전략', '투자자 설득을 위한 발표 기법', 'INTERMEDIATE', 'ONLINE', 8,
 (SELECT id FROM cm_categories WHERE code = 'funding' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 45 DAY, NOW(), 0),
(3, '투자 계약의 이해', 'Term Sheet와 투자 조건 분석', 'ADVANCED', 'ONLINE', 10,
 (SELECT id FROM cm_categories WHERE code = 'funding' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 42 DAY, NOW(), 0),
(3, '시드 펀딩 전략', '초기 투자 유치 가이드', 'BEGINNER', 'BLENDED', 12,
 (SELECT id FROM cm_categories WHERE code = 'funding' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 40 DAY, NOW(), 0),
(3, '밸류에이션 기초', '스타트업 가치 평가 방법', 'INTERMEDIATE', 'ONLINE', 6,
 (SELECT id FROM cm_categories WHERE code = 'funding' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 38 DAY, NOW(), 0);

-- =============================================
-- 19. 테넌트 2 스냅샷 데이터 (주요 코스)
-- =============================================
INSERT INTO cm_snapshots (tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
(2, (SELECT id FROM cm_courses WHERE title = '영업 기초 이론' AND tenant_id = 2),
 '영업 기초 이론 v1', 'B2B 영업의 기본 개념과 프로세스', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 25 DAY, NOW()),
(2, (SELECT id FROM cm_courses WHERE title = '팀 리더십 기초' AND tenant_id = 2),
 '팀 리더십 기초 v1', '효과적인 팀 운영과 리더십 스킬', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 30 DAY, NOW()),
(2, (SELECT id FROM cm_courses WHERE title = '신입사원 OJT' AND tenant_id = 2),
 '신입사원 OJT v1', '회사 적응을 위한 기본 교육', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 40 DAY, NOW()),
(2, (SELECT id FROM cm_courses WHERE title = '정보보안 기초' AND tenant_id = 2),
 '정보보안 기초 v1', '정보 보안 인식 제고 교육', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 45 DAY, NOW()),
(2, (SELECT id FROM cm_courses WHERE title = '협상 스킬 마스터' AND tenant_id = 2),
 '협상 스킬 마스터 v1', '효과적인 협상 전략과 기법', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 23 DAY, NOW()),
(2, (SELECT id FROM cm_courses WHERE title = '개인정보보호법' AND tenant_id = 2),
 '개인정보보호법 v1', '개인정보 처리 및 보호 의무', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 43 DAY, NOW());

-- =============================================
-- 20. 테넌트 3 스냅샷 데이터 (주요 코스)
-- =============================================
INSERT INTO cm_snapshots (tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
(3, (SELECT id FROM cm_courses WHERE title = '스타트업 101' AND tenant_id = 3),
 '스타트업 101 v1', '스타트업 창업의 기본 개념', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 25 DAY, NOW()),
(3, (SELECT id FROM cm_courses WHERE title = '디지털 마케팅 기초' AND tenant_id = 3),
 '디지털 마케팅 기초 v1', '온라인 마케팅 전략 입문', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 30 DAY, NOW()),
(3, (SELECT id FROM cm_courses WHERE title = '프로덕트 매니지먼트' AND tenant_id = 3),
 '프로덕트 매니지먼트 v1', 'PM 역할과 핵심 역량', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 35 DAY, NOW()),
(3, (SELECT id FROM cm_courses WHERE title = '그로스해킹 기초' AND tenant_id = 3),
 '그로스해킹 기초 v1', '데이터 기반 성장 전략', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 15 DAY, NOW()),
(3, (SELECT id FROM cm_courses WHERE title = 'IR 피칭 전략' AND tenant_id = 3),
 'IR 피칭 전략 v1', '투자자 설득을 위한 발표 기법', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 40 DAY, NOW()),
(3, (SELECT id FROM cm_courses WHERE title = '린 스타트업 방법론' AND tenant_id = 3),
 '린 스타트업 방법론 v1', '빠른 실험과 검증 기반 성장', 'ACTIVE', 1,
 (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 23 DAY, NOW());

-- =============================================
-- 21. 테넌트 2 프로그램 데이터 (APPROVED 상태) - cm_programs 테이블 제거로 비활성화
-- =============================================
/*
INSERT INTO cm_programs (tenant_id, title, description, level, type, estimated_hours, snapshot_id, status, created_by, submitted_at, approved_by, approved_at, rejection_reason, rejected_at, created_at, updated_at, version) VALUES
(2, '영업 기초 이론 과정', 'B2B 영업의 기본 개념과 프로세스를 학습합니다.', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '영업 기초 이론 v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 20 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 18 DAY, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0),
(2, '팀 리더십 기초 과정', '효과적인 팀 운영과 리더십 스킬을 개발합니다.', 'BEGINNER', 'ONLINE', 12,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '팀 리더십 기초 v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 25 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 23 DAY, NULL, NULL, NOW() - INTERVAL 30 DAY, NOW(), 0),
(2, '신입사원 OJT 과정', '신입사원을 위한 회사 적응 종합 교육입니다.', 'BEGINNER', 'BLENDED', 40,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '신입사원 OJT v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 35 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 33 DAY, NULL, NULL, NOW() - INTERVAL 40 DAY, NOW(), 0),
(2, '정보보안 기초 과정', '전 직원 필수 정보보안 교육입니다.', 'BEGINNER', 'ONLINE', 3,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '정보보안 기초 v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 40 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 38 DAY, NULL, NULL, NOW() - INTERVAL 45 DAY, NOW(), 0),
(2, '협상 스킬 마스터 과정', '영업 협상력 향상을 위한 실전 교육입니다.', 'INTERMEDIATE', 'ONLINE', 15,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '협상 스킬 마스터 v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), NOW() - INTERVAL 18 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 16 DAY, NULL, NULL, NOW() - INTERVAL 23 DAY, NOW(), 0),
(2, '개인정보보호법 과정', '개인정보 처리 및 보호 의무 교육입니다.', 'BEGINNER', 'ONLINE', 4,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '개인정보보호법 v1' AND tenant_id = 2),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@company-a.com'), NOW() - INTERVAL 38 DAY, (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 36 DAY, NULL, NULL, NOW() - INTERVAL 43 DAY, NOW(), 0);
*/

-- =============================================
-- 22. 테넌트 3 프로그램 데이터 (APPROVED 상태) - cm_programs 테이블 제거로 비활성화
-- =============================================
/*
INSERT INTO cm_programs (tenant_id, title, description, level, type, estimated_hours, snapshot_id, status, created_by, submitted_at, approved_by, approved_at, rejection_reason, rejected_at, created_at, updated_at, version) VALUES
(3, '스타트업 101 과정', '스타트업 창업의 기본을 배웁니다.', 'BEGINNER', 'ONLINE', 8,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '스타트업 101 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 20 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 18 DAY, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0),
(3, '디지털 마케팅 기초 과정', '온라인 마케팅의 기본 전략을 학습합니다.', 'BEGINNER', 'ONLINE', 15,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '디지털 마케팅 기초 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 25 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 23 DAY, NULL, NULL, NOW() - INTERVAL 30 DAY, NOW(), 0),
(3, '프로덕트 매니지먼트 과정', 'PM 역할과 핵심 역량을 개발합니다.', 'INTERMEDIATE', 'ONLINE', 20,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '프로덕트 매니지먼트 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 30 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 28 DAY, NULL, NULL, NOW() - INTERVAL 35 DAY, NOW(), 0),
(3, '그로스해킹 기초 과정', '데이터 기반 성장 전략을 배웁니다.', 'BEGINNER', 'ONLINE', 10,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '그로스해킹 기초 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 10 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 8 DAY, NULL, NULL, NOW() - INTERVAL 15 DAY, NOW(), 0),
(3, 'IR 피칭 전략 과정', '투자자 설득을 위한 발표 기법을 학습합니다.', 'INTERMEDIATE', 'ONLINE', 8,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = 'IR 피칭 전략 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), NOW() - INTERVAL 35 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 33 DAY, NULL, NULL, NOW() - INTERVAL 40 DAY, NOW(), 0),
(3, '린 스타트업 방법론 과정', '빠른 실험과 검증 기반 성장 방법을 배웁니다.', 'INTERMEDIATE', 'ONLINE', 12,
 (SELECT id FROM cm_snapshots WHERE snapshot_name = '린 스타트업 방법론 v1' AND tenant_id = 3),
 'APPROVED', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), NOW() - INTERVAL 18 DAY, (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW() - INTERVAL 16 DAY, NULL, NULL, NOW() - INTERVAL 23 DAY, NOW(), 0);
*/

-- =============================================
-- 23. 테넌트 2 차수 데이터
-- =============================================
INSERT INTO course_times (
    tenant_id, course_id, title, delivery_type, status,
    enroll_start_date, enroll_end_date, class_start_date, class_end_date,
    capacity, max_waiting_count, current_enrollment, enrollment_method,
    min_progress_for_completion, price, is_free, allow_late_enrollment,
    created_by, created_at, updated_at, version
) VALUES
-- 영업 기초 이론
(2, (SELECT id FROM cm_courses WHERE title = '영업 기초 이론 과정' AND tenant_id = 2),
 '영업 기초 이론 1차', 'ONLINE', 'ONGOING',
 '2025-12-01', '2025-12-20', '2025-12-25', '2026-01-25',
 50, 10, 35, 'FIRST_COME', 80, 0.00, true, true,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0),
-- 팀 리더십 기초
(2, (SELECT id FROM cm_courses WHERE title = '팀 리더십 기초 과정' AND tenant_id = 2),
 '팀 리더십 기초 1차', 'ONLINE', 'RECRUITING',
 '2026-01-01', '2026-01-15', '2026-01-20', '2026-02-20',
 30, 5, 18, 'APPROVAL', 85, 50000.00, false, false,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0),
-- 신입사원 OJT
(2, (SELECT id FROM cm_courses WHERE title = '신입사원 OJT 과정' AND tenant_id = 2),
 '신입사원 OJT 2026년 1분기', 'BLENDED', 'ONGOING',
 '2025-12-15', '2025-12-31', '2026-01-02', '2026-02-28',
 20, 0, 20, 'INVITE_ONLY', 90, 0.00, true, false,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0),
-- 정보보안 기초
(2, (SELECT id FROM cm_courses WHERE title = '정보보안 기초 과정' AND tenant_id = 2),
 '정보보안 기초 2026년', 'ONLINE', 'ONGOING',
 '2026-01-01', '2026-12-31', '2026-01-01', '2026-12-31',
 500, 0, 120, 'FIRST_COME', 100, 0.00, true, true,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0),
-- 협상 스킬 마스터
(2, (SELECT id FROM cm_courses WHERE title = '협상 스킬 마스터 과정' AND tenant_id = 2),
 '협상 스킬 마스터 1차', 'ONLINE', 'CLOSED',
 '2025-10-01', '2025-10-15', '2025-10-20', '2025-12-20',
 25, 5, 22, 'FIRST_COME', 80, 80000.00, false, false,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0),
-- 개인정보보호법
(2, (SELECT id FROM cm_courses WHERE title = '개인정보보호법 과정' AND tenant_id = 2),
 '개인정보보호법 2026년', 'ONLINE', 'ONGOING',
 '2026-01-01', '2026-12-31', '2026-01-01', '2026-12-31',
 500, 0, 95, 'FIRST_COME', 100, 0.00, true, true,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW(), NOW(), 0);

-- =============================================
-- 24. 테넌트 3 차수 데이터
-- =============================================
INSERT INTO course_times (
    tenant_id, course_id, title, delivery_type, status,
    enroll_start_date, enroll_end_date, class_start_date, class_end_date,
    capacity, max_waiting_count, current_enrollment, enrollment_method,
    min_progress_for_completion, price, is_free, allow_late_enrollment,
    created_by, created_at, updated_at, version
) VALUES
-- 스타트업 101
(3, (SELECT id FROM cm_courses WHERE title = '스타트업 101 과정' AND tenant_id = 3),
 '스타트업 101 1차', 'ONLINE', 'ONGOING',
 '2025-12-01', '2025-12-20', '2025-12-25', '2026-01-25',
 100, 20, 67, 'FIRST_COME', 70, 0.00, true, true,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0),
-- 디지털 마케팅 기초
(3, (SELECT id FROM cm_courses WHERE title = '디지털 마케팅 기초 과정' AND tenant_id = 3),
 '디지털 마케팅 기초 1차', 'ONLINE', 'RECRUITING',
 '2026-01-01', '2026-01-20', '2026-01-25', '2026-03-25',
 50, 10, 32, 'FIRST_COME', 75, 30000.00, false, true,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0),
-- 프로덕트 매니지먼트
(3, (SELECT id FROM cm_courses WHERE title = '프로덕트 매니지먼트 과정' AND tenant_id = 3),
 '프로덕트 매니지먼트 1차', 'ONLINE', 'ONGOING',
 '2025-11-15', '2025-12-15', '2025-12-20', '2026-02-20',
 40, 5, 38, 'APPROVAL', 80, 100000.00, false, false,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0),
-- 그로스해킹 기초
(3, (SELECT id FROM cm_courses WHERE title = '그로스해킹 기초 과정' AND tenant_id = 3),
 '그로스해킹 기초 1차', 'ONLINE', 'RECRUITING',
 '2026-01-05', '2026-01-25', '2026-02-01', '2026-03-01',
 60, 10, 25, 'FIRST_COME', 75, 50000.00, false, true,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0),
-- IR 피칭 전략
(3, (SELECT id FROM cm_courses WHERE title = 'IR 피칭 전략 과정' AND tenant_id = 3),
 'IR 피칭 전략 1차', 'ONLINE', 'CLOSED',
 '2025-09-01', '2025-09-20', '2025-10-01', '2025-11-30',
 30, 5, 28, 'APPROVAL', 85, 150000.00, false, false,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0),
-- 린 스타트업 방법론
(3, (SELECT id FROM cm_courses WHERE title = '린 스타트업 방법론 과정' AND tenant_id = 3),
 '린 스타트업 방법론 1차', 'ONLINE', 'ONGOING',
 '2025-12-10', '2025-12-25', '2026-01-01', '2026-02-15',
 45, 8, 42, 'FIRST_COME', 80, 70000.00, false, true,
 (SELECT id FROM users WHERE email = 'operator1@company-b.com'), NOW(), NOW(), 0);

-- =============================================
-- 25. 커뮤니티 게시글 데이터 (테넌트별 20개)
-- =============================================
-- 테넌트 1 커뮤니티 게시글
INSERT INTO community_posts (tenant_id, type, category, title, content, author_id, view_count, is_pinned, is_solved, is_private, tags, created_at, updated_at) VALUES
(1, 'QUESTION', '개발 질문', 'Spring Boot 시작하기 질문', 'Spring Boot 프로젝트를 처음 시작하는데 어떤 의존성을 추가해야 할까요?', (SELECT id FROM users WHERE email = 'user1@default.com'), 45, false, false, false, 'spring,boot,시작', NOW() - INTERVAL 10 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'JPA N+1 문제 해결 방법', 'JPA에서 N+1 문제를 겪고 있는데 fetch join 외에 다른 방법이 있을까요?', (SELECT id FROM users WHERE email = 'user2@default.com'), 128, false, true, false, 'jpa,n+1,성능', NOW() - INTERVAL 9 DAY, NOW()),
(1, 'TIP', '학습 팁', 'React Hooks 사용 팁 공유', 'useCallback과 useMemo의 차이점과 적절한 사용 시점을 공유합니다.', (SELECT id FROM users WHERE email = 'user3@default.com'), 256, true, false, false, 'react,hooks,성능최적화', NOW() - INTERVAL 8 DAY, NOW()),
(1, 'TIP', '학습 팁', 'AWS EC2 비용 절감 방법', 'Reserved Instance와 Spot Instance를 활용한 비용 절감 경험을 공유합니다.', (SELECT id FROM users WHERE email = 'user4@default.com'), 189, false, false, false, 'aws,ec2,비용절감', NOW() - INTERVAL 7 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'Kubernetes Pod 배포 오류', 'ImagePullBackOff 에러가 발생하는데 원인이 뭘까요?', (SELECT id FROM users WHERE email = 'user5@default.com'), 67, false, false, false, 'kubernetes,pod,오류', NOW() - INTERVAL 6 DAY, NOW()),
(1, 'TIP', '학습 팁', 'SQL 쿼리 최적화 사례', '실제 프로젝트에서 쿼리 실행 시간을 10초에서 0.5초로 줄인 경험입니다.', (SELECT id FROM users WHERE email = 'user6@default.com'), 312, true, false, false, 'sql,최적화,성능', NOW() - INTERVAL 5 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'TypeScript 타입 추론 질문', 'Generic을 사용할 때 타입 추론이 제대로 안 되는 경우가 있어요.', (SELECT id FROM users WHERE email = 'user7@default.com'), 34, false, false, false, 'typescript,generic,타입', NOW() - INTERVAL 4 DAY, NOW()),
(1, 'TIP', '학습 팁', 'Docker Compose 설정 공유', '개발 환경을 위한 Docker Compose 설정 템플릿을 공유합니다.', (SELECT id FROM users WHERE email = 'user8@default.com'), 145, false, false, false, 'docker,compose,개발환경', NOW() - INTERVAL 3 DAY, NOW()),
(1, 'REVIEW', '강의 후기', 'CI/CD 파이프라인 구축 후기', 'GitHub Actions로 CI/CD 파이프라인을 구축한 경험을 공유합니다.', (SELECT id FROM users WHERE email = 'user9@default.com'), 98, false, false, false, 'cicd,github,actions', NOW() - INTERVAL 2 DAY, NOW()),
(1, 'TIP', '학습 팁', 'Spring Security JWT 구현', 'JWT 기반 인증을 구현하면서 겪은 삽질기입니다.', (SELECT id FROM users WHERE email = 'user10@default.com'), 234, false, false, false, 'spring,security,jwt', NOW() - INTERVAL 1 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'Redis 캐싱 전략', '언제 Redis를 사용해야 하고, 어떤 캐싱 전략이 좋을까요?', (SELECT id FROM users WHERE email = 'user11@default.com'), 78, false, true, false, 'redis,캐싱,전략', NOW() - INTERVAL 12 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'React Query vs SWR', 'React Query와 SWR 중 어떤 것을 선택해야 할까요?', (SELECT id FROM users WHERE email = 'user12@default.com'), 156, false, true, false, 'react,query,swr', NOW() - INTERVAL 11 DAY, NOW()),
(1, 'REVIEW', '강의 후기', 'Clean Architecture 도입기', '프로젝트에 Clean Architecture를 도입한 경험입니다.', (SELECT id FROM users WHERE email = 'user13@default.com'), 267, true, false, false, 'clean,architecture,설계', NOW() - INTERVAL 15 DAY, NOW()),
(1, 'TIP', '학습 팁', 'GraphQL vs REST API', '프로젝트 상황에 따른 API 설계 선택 기준을 공유합니다.', (SELECT id FROM users WHERE email = 'user14@default.com'), 189, false, false, false, 'graphql,rest,api', NOW() - INTERVAL 14 DAY, NOW()),
(1, 'REVIEW', '강의 후기', 'MSA 도입 실패 경험', '작은 팀에서 MSA를 도입했다가 실패한 경험을 공유합니다.', (SELECT id FROM users WHERE email = 'user15@default.com'), 423, true, false, false, 'msa,아키텍처,실패', NOW() - INTERVAL 13 DAY, NOW()),
(1, 'TIP', '학습 팁', 'Webpack 번들 사이즈 줄이기', 'Tree shaking과 code splitting으로 번들 사이즈를 줄인 방법입니다.', (SELECT id FROM users WHERE email = 'user16@default.com'), 112, false, false, false, 'webpack,번들,최적화', NOW() - INTERVAL 16 DAY, NOW()),
(1, 'TIP', '학습 팁', 'Java Stream API 활용', 'Stream API를 효과적으로 사용하는 패턴을 공유합니다.', (SELECT id FROM users WHERE email = 'user17@default.com'), 98, false, false, false, 'java,stream,api', NOW() - INTERVAL 17 DAY, NOW()),
(1, 'TIP', '학습 팁', 'VS Code 생산성 향상 팁', '개발 생산성을 높이는 VS Code 설정과 확장 프로그램입니다.', (SELECT id FROM users WHERE email = 'user18@default.com'), 345, false, false, false, 'vscode,생산성,확장', NOW() - INTERVAL 18 DAY, NOW()),
(1, 'QUESTION', '개발 질문', 'Git 브랜치 전략 질문', 'Git Flow vs GitHub Flow 중 어떤 것이 좋을까요?', (SELECT id FROM users WHERE email = 'user19@default.com'), 87, false, false, false, 'git,브랜치,전략', NOW() - INTERVAL 19 DAY, NOW()),
(1, 'REVIEW', '강의 후기', '신입 개발자 취업 후기', '비전공자로 개발자 취업에 성공한 이야기입니다.', (SELECT id FROM users WHERE email = 'user20@default.com'), 567, true, false, false, '취업,비전공,성공', NOW() - INTERVAL 20 DAY, NOW());

-- 테넌트 2 커뮤니티 게시글
INSERT INTO community_posts (tenant_id, type, category, title, content, author_id, view_count, is_pinned, is_solved, is_private, tags, created_at, updated_at) VALUES
(2, 'TIP', '업무 팁', '영업 미팅 준비 팁', '고객 미팅 전 꼭 확인해야 할 체크리스트를 공유합니다.', (SELECT id FROM users WHERE email = 'user1@company-a.com'), 89, true, false, false, '영업,미팅,체크리스트', NOW() - INTERVAL 10 DAY, NOW()),
(2, 'TIP', '업무 팁', '협상에서 이기는 법', '최근 협상에서 좋은 결과를 얻은 경험을 공유합니다.', (SELECT id FROM users WHERE email = 'user2@company-a.com'), 156, false, false, false, '협상,커뮤니케이션', NOW() - INTERVAL 9 DAY, NOW()),
(2, 'QUESTION', '신입 질문', '신입사원 적응 질문', '입사 첫 주인데 무엇부터 해야 할까요?', (SELECT id FROM users WHERE email = 'user3@company-a.com'), 45, false, true, false, '신입,적응,조언', NOW() - INTERVAL 8 DAY, NOW()),
(2, 'TIP', '업무 팁', '팀 회의 효율화 방법', '회의 시간을 줄이고 생산성을 높인 방법입니다.', (SELECT id FROM users WHERE email = 'user4@company-a.com'), 123, false, false, false, '회의,효율,생산성', NOW() - INTERVAL 7 DAY, NOW()),
(2, 'TIP', '업무 팁', '보고서 작성 템플릿', '효과적인 업무 보고서 템플릿을 공유합니다.', (SELECT id FROM users WHERE email = 'user5@company-a.com'), 234, true, false, false, '보고서,템플릿,문서', NOW() - INTERVAL 6 DAY, NOW()),
(2, 'TIP', '업무 팁', '재택근무 생산성 유지', '재택근무 중 집중력을 유지하는 방법입니다.', (SELECT id FROM users WHERE email = 'user6@company-a.com'), 178, false, false, false, '재택근무,집중력,생산성', NOW() - INTERVAL 5 DAY, NOW()),
(2, 'QUESTION', '커리어', '동료와의 갈등 해결', '팀원과 의견 충돌이 있을 때 어떻게 해결하나요?', (SELECT id FROM users WHERE email = 'user7@company-a.com'), 67, false, true, false, '갈등,팀워크,소통', NOW() - INTERVAL 4 DAY, NOW()),
(2, 'TIP', '업무 팁', '프레젠테이션 긴장 극복', '발표할 때 긴장을 줄이는 방법을 공유합니다.', (SELECT id FROM users WHERE email = 'user8@company-a.com'), 145, false, false, false, '발표,긴장,프레젠테이션', NOW() - INTERVAL 3 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '고객 클레임 대응', '까다로운 고객의 클레임을 해결한 경험입니다.', (SELECT id FROM users WHERE email = 'user9@company-a.com'), 98, false, false, false, '고객,클레임,대응', NOW() - INTERVAL 2 DAY, NOW()),
(2, 'TIP', '업무 팁', '업무 우선순위 정하기', '할 일이 많을 때 우선순위를 정하는 방법입니다.', (SELECT id FROM users WHERE email = 'user10@company-a.com'), 112, false, false, false, '우선순위,시간관리', NOW() - INTERVAL 1 DAY, NOW()),
(2, 'TIP', '업무 팁', '엑셀 단축키 모음', '업무에 유용한 엑셀 단축키를 정리했습니다.', (SELECT id FROM users WHERE email = 'user11@company-a.com'), 289, true, false, false, '엑셀,단축키,생산성', NOW() - INTERVAL 11 DAY, NOW()),
(2, 'TIP', '업무 팁', '이메일 작성 에티켓', '비즈니스 이메일 작성 시 주의할 점입니다.', (SELECT id FROM users WHERE email = 'user12@company-a.com'), 167, false, false, false, '이메일,에티켓,비즈니스', NOW() - INTERVAL 12 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '연봉 협상 후기', '이번 연봉 협상에서 배운 점을 공유합니다.', (SELECT id FROM users WHERE email = 'user13@company-a.com'), 345, false, false, false, '연봉,협상,후기', NOW() - INTERVAL 13 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '팀장이 되고 나서', '처음 팀장이 되었을 때 겪은 어려움과 해결 방법입니다.', (SELECT id FROM users WHERE email = 'user14@company-a.com'), 234, false, false, false, '팀장,리더십,경험', NOW() - INTERVAL 14 DAY, NOW()),
(2, 'TIP', '업무 팁', '점심시간 활용법', '점심시간을 효과적으로 활용하는 방법입니다.', (SELECT id FROM users WHERE email = 'user15@company-a.com'), 78, false, false, false, '점심,휴식,활용', NOW() - INTERVAL 15 DAY, NOW()),
(2, 'TIP', '업무 팁', '업무 스트레스 관리', '스트레스를 건강하게 해소하는 방법을 공유합니다.', (SELECT id FROM users WHERE email = 'user16@company-a.com'), 198, false, false, false, '스트레스,건강,관리', NOW() - INTERVAL 16 DAY, NOW()),
(2, 'TIP', '업무 팁', '효과적인 피드백 방법', '팀원에게 피드백을 줄 때 주의할 점입니다.', (SELECT id FROM users WHERE email = 'user17@company-a.com'), 145, false, false, false, '피드백,팀워크,소통', NOW() - INTERVAL 17 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '사내 정치 생존기', '회사 생활에서 관계를 잘 유지하는 방법입니다.', (SELECT id FROM users WHERE email = 'user18@company-a.com'), 423, true, false, false, '회사생활,관계,정치', NOW() - INTERVAL 18 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '워라밸 찾기', '일과 삶의 균형을 찾은 경험을 공유합니다.', (SELECT id FROM users WHERE email = 'user19@company-a.com'), 256, false, false, false, '워라밸,균형,삶', NOW() - INTERVAL 19 DAY, NOW()),
(2, 'REVIEW', '경험 공유', '면접관 되어보니', '처음 면접관으로 참여한 경험입니다.', (SELECT id FROM users WHERE email = 'user20@company-a.com'), 178, false, false, false, '면접,채용,경험', NOW() - INTERVAL 20 DAY, NOW());

-- 테넌트 3 커뮤니티 게시글
INSERT INTO community_posts (tenant_id, type, category, title, content, author_id, view_count, is_pinned, is_solved, is_private, tags, created_at, updated_at) VALUES
(3, 'REVIEW', '창업 후기', '스타트업 첫 투자 유치 후기', '시드 투자를 받기까지의 여정을 공유합니다.', (SELECT id FROM users WHERE email = 'user1@company-b.com'), 456, true, false, false, '투자,시드,유치', NOW() - INTERVAL 10 DAY, NOW()),
(3, 'TIP', '창업 팁', 'MVP 빠르게 만들기', '2주 만에 MVP를 완성한 경험입니다.', (SELECT id FROM users WHERE email = 'user2@company-b.com'), 234, false, false, false, 'mvp,개발,빠른실행', NOW() - INTERVAL 9 DAY, NOW()),
(3, 'QUESTION', '창업 질문', '창업팀 구성 조언 요청', '공동창업자를 어떻게 구해야 할까요?', (SELECT id FROM users WHERE email = 'user3@company-b.com'), 123, false, true, false, '창업팀,공동창업자,채용', NOW() - INTERVAL 8 DAY, NOW()),
(3, 'REVIEW', '창업 후기', 'PMF 찾기 실패담', 'Product-Market Fit을 찾지 못해 피봇한 경험입니다.', (SELECT id FROM users WHERE email = 'user4@company-b.com'), 345, true, false, false, 'pmf,피봇,실패', NOW() - INTERVAL 7 DAY, NOW()),
(3, 'TIP', '마케팅', '앱 스토어 최적화 팁', 'ASO로 다운로드를 3배 늘린 방법입니다.', (SELECT id FROM users WHERE email = 'user5@company-b.com'), 189, false, false, false, 'aso,앱스토어,마케팅', NOW() - INTERVAL 6 DAY, NOW()),
(3, 'TIP', '고객 개발', '고객 인터뷰 진행법', '효과적인 고객 인터뷰 방법을 공유합니다.', (SELECT id FROM users WHERE email = 'user6@company-b.com'), 156, false, false, false, '고객,인터뷰,리서치', NOW() - INTERVAL 5 DAY, NOW()),
(3, 'TIP', '투자', '데모데이 발표 팁', '데모데이에서 눈에 띄는 발표 방법입니다.', (SELECT id FROM users WHERE email = 'user7@company-b.com'), 212, false, false, false, '데모데이,발표,피칭', NOW() - INTERVAL 4 DAY, NOW()),
(3, 'REVIEW', '창업 후기', '스타트업 실패 원인', '첫 창업이 실패한 이유를 분석해봤습니다.', (SELECT id FROM users WHERE email = 'user8@company-b.com'), 567, true, false, false, '실패,분석,교훈', NOW() - INTERVAL 3 DAY, NOW()),
(3, 'QUESTION', '창업 질문', '부트스트래핑 vs 투자', '자금 조달 방법에 대한 고민입니다.', (SELECT id FROM users WHERE email = 'user9@company-b.com'), 98, false, false, false, '부트스트래핑,투자,자금', NOW() - INTERVAL 2 DAY, NOW()),
(3, 'TIP', '마케팅', '그로스해킹 실전 사례', '실제로 효과가 있었던 그로스해킹 전략입니다.', (SELECT id FROM users WHERE email = 'user10@company-b.com'), 278, false, false, false, '그로스해킹,성장,전략', NOW() - INTERVAL 1 DAY, NOW()),
(3, 'TIP', '채용', '초기 팀 채용 전략', '스타트업 초기에 인재를 영입하는 방법입니다.', (SELECT id FROM users WHERE email = 'user11@company-b.com'), 167, false, false, false, '채용,인재,팀빌딩', NOW() - INTERVAL 11 DAY, NOW()),
(3, 'TIP', '투자', '피칭 덱 작성 가이드', '투자자를 사로잡는 피칭 덱 만들기입니다.', (SELECT id FROM users WHERE email = 'user12@company-b.com'), 345, false, false, false, '피칭,덱,투자', NOW() - INTERVAL 12 DAY, NOW()),
(3, 'TIP', '창업 팁', '린 캔버스 활용법', '린 캔버스로 사업 모델을 검증한 경험입니다.', (SELECT id FROM users WHERE email = 'user13@company-b.com'), 134, false, false, false, '린캔버스,비즈니스모델', NOW() - INTERVAL 13 DAY, NOW()),
(3, 'REVIEW', '창업 후기', '공동창업자와의 갈등', '공동창업자와 의견 차이를 해결한 방법입니다.', (SELECT id FROM users WHERE email = 'user14@company-b.com'), 234, false, false, false, '공동창업자,갈등,해결', NOW() - INTERVAL 14 DAY, NOW()),
(3, 'QUESTION', '창업 질문', 'B2B vs B2C 선택', 'B2B와 B2C 중 어떤 모델이 좋을까요?', (SELECT id FROM users WHERE email = 'user15@company-b.com'), 112, false, true, false, 'b2b,b2c,비즈니스모델', NOW() - INTERVAL 15 DAY, NOW()),
(3, 'TIP', '법무/재무', '법인 설립 가이드', '스타트업 법인 설립 절차를 정리했습니다.', (SELECT id FROM users WHERE email = 'user16@company-b.com'), 198, false, false, false, '법인,설립,절차', NOW() - INTERVAL 16 DAY, NOW()),
(3, 'QUESTION', '창업 질문', '지분 구조 설계', '창업팀 지분 배분에 대한 조언을 구합니다.', (SELECT id FROM users WHERE email = 'user17@company-b.com'), 145, false, false, false, '지분,배분,구조', NOW() - INTERVAL 17 DAY, NOW()),
(3, 'TIP', '마케팅', '초기 마케팅 예산 책정', '적은 예산으로 마케팅하는 방법입니다.', (SELECT id FROM users WHERE email = 'user18@company-b.com'), 178, false, false, false, '마케팅,예산,저비용', NOW() - INTERVAL 18 DAY, NOW()),
(3, 'TIP', '창업 팁', '스타트업 멘토 찾기', '좋은 멘토를 찾는 방법을 공유합니다.', (SELECT id FROM users WHERE email = 'user19@company-b.com'), 123, false, false, false, '멘토,네트워킹,조언', NOW() - INTERVAL 19 DAY, NOW()),
(3, 'TIP', '투자', '시리즈 A 준비 체크리스트', '시리즈 A 투자 유치를 위한 준비사항입니다.', (SELECT id FROM users WHERE email = 'user20@company-b.com'), 289, true, false, false, '시리즈A,투자,체크리스트', NOW() - INTERVAL 20 DAY, NOW());

-- =============================================
-- 26. 리뷰 데이터 (주요 차수별)
-- =============================================
-- 테넌트 1: Spring Boot 기초 3차 리뷰 (ONGOING 차수)
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user1@default.com'), 5, '강의 내용이 정말 알차고 실무에 바로 적용할 수 있어서 좋았습니다.', 100, NOW() - INTERVAL 10 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user2@default.com'), 4, '기초부터 차근차근 설명해주셔서 이해하기 쉬웠습니다.', 85, NOW() - INTERVAL 9 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user3@default.com'), 5, '실습 예제가 많아서 직접 따라하며 배울 수 있었습니다.', 90, NOW() - INTERVAL 8 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user4@default.com'), 4, '강사님의 설명이 명확하고 질문에 빠르게 답변해주셨습니다.', 75, NOW() - INTERVAL 7 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user5@default.com'), 3, '전반적으로 만족스러운 강의였습니다. 다음 심화 과정도 기대됩니다.', 60, NOW() - INTERVAL 6 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user6@default.com'), 5, 'Spring Boot의 핵심 개념을 완벽하게 이해할 수 있었습니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user7@default.com'), 4, '실무에서 바로 쓸 수 있는 내용이 많아 좋았습니다.', 80, NOW() - INTERVAL 4 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user8@default.com'), 5, '초보자도 쉽게 따라갈 수 있는 강의입니다.', 95, NOW() - INTERVAL 3 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user9@default.com'), 4, '프로젝트 구성부터 배포까지 전반적인 내용을 다루어 좋았습니다.', 70, NOW() - INTERVAL 2 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 3차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user10@default.com'), 5, '재수강하고 싶은 강의입니다!', 100, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- 테넌트 1: Java 프로그래밍 마스터 1차 리뷰 (ONGOING 차수)
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user11@default.com'), 5, 'Java의 핵심 개념을 체계적으로 배울 수 있었습니다.', 100, NOW() - INTERVAL 15 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user12@default.com'), 5, '객체지향 프로그래밍에 대한 이해도가 높아졌습니다.', 95, NOW() - INTERVAL 14 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user13@default.com'), 4, '코드 리뷰가 포함되어 있어서 실력 향상에 도움이 됐습니다.', 80, NOW() - INTERVAL 13 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user14@default.com'), 5, '실무에서 자주 사용하는 패턴을 배울 수 있어서 좋았습니다.', 90, NOW() - INTERVAL 12 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user15@default.com'), 4, '난이도가 적절하고 커리큘럼이 잘 구성되어 있습니다.', 75, NOW() - INTERVAL 11 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user16@default.com'), 5, 'Java 21의 새로운 기능도 다루어서 유익했습니다.', 100, NOW() - INTERVAL 10 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user17@default.com'), 4, '기초부터 심화까지 체계적인 학습이 가능했습니다.', 85, NOW() - INTERVAL 9 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user18@default.com'), 5, '예제 코드가 풍부하고 이해하기 쉬웠습니다.', 95, NOW() - INTERVAL 8 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user19@default.com'), 4, '실습 위주의 강의라서 좋았습니다.', 70, NOW() - INTERVAL 7 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user20@default.com'), 5, 'Java 개발자 필수 강의!', 100, NOW() - INTERVAL 6 DAY, NOW(), 0);

-- 테넌트 2: 영업 기초 이론 1차 리뷰 (ONGOING 차수)
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user1@company-a.com'), 5, '영업 기초를 체계적으로 배울 수 있어서 좋았습니다.', 100, NOW() - INTERVAL 10 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user2@company-a.com'), 4, '실제 영업 사례가 많아서 이해하기 쉬웠습니다.', 85, NOW() - INTERVAL 9 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user3@company-a.com'), 5, '고객 응대 스킬이 많이 향상되었습니다.', 90, NOW() - INTERVAL 8 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user4@company-a.com'), 4, '협상 기술에 대한 내용이 특히 유익했습니다.', 75, NOW() - INTERVAL 7 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user5@company-a.com'), 5, '신입 영업사원에게 꼭 필요한 내용입니다.', 95, NOW() - INTERVAL 6 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user6@company-a.com'), 5, '현장에서 바로 적용할 수 있는 실용적인 강의입니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user7@company-a.com'), 4, '강사님의 실무 경험이 녹아있어 좋았습니다.', 80, NOW() - INTERVAL 4 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user8@company-a.com'), 5, '영업 자신감이 생겼습니다!', 90, NOW() - INTERVAL 3 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user9@company-a.com'), 4, '기본기를 탄탄하게 다질 수 있었습니다.', 70, NOW() - INTERVAL 2 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user10@company-a.com'), 5, '추천합니다!', 100, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- 테넌트 3: 스타트업 101 1차 리뷰 (ONGOING 차수)
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user1@company-b.com'), 5, '창업을 준비하는 분들에게 강력 추천합니다.', 100, NOW() - INTERVAL 10 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user2@company-b.com'), 5, '린 스타트업 방법론을 실제로 적용할 수 있게 되었습니다.', 95, NOW() - INTERVAL 9 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user3@company-b.com'), 4, 'MVP 개념을 명확히 이해하게 되었습니다.', 80, NOW() - INTERVAL 8 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user4@company-b.com'), 5, '사업 계획서 작성에 많은 도움이 되었습니다.', 90, NOW() - INTERVAL 7 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user5@company-b.com'), 4, '실제 창업가의 경험담이 인상적이었습니다.', 75, NOW() - INTERVAL 6 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user6@company-b.com'), 5, '투자 유치 과정을 상세히 알 수 있었습니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user7@company-b.com'), 5, 'PMF를 찾는 방법을 배웠습니다.', 95, NOW() - INTERVAL 4 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user8@company-b.com'), 4, '창업 전 필수로 들어야 할 강의입니다.', 85, NOW() - INTERVAL 3 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user9@company-b.com'), 5, '멘토링도 받을 수 있어서 좋았습니다.', 100, NOW() - INTERVAL 2 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user10@company-b.com'), 5, '최고의 창업 강의!', 100, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- =============================================
-- 27. 장바구니 데이터 (테넌트 1 기준 5개)
-- =============================================
INSERT INTO cart_items (tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(1, (SELECT id FROM users WHERE email = 'user21@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 2차' AND tenant_id = 1),
 NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user22@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user23@default.com'),
 (SELECT id FROM course_times WHERE title = 'AWS 클라우드 아키텍처 2차' AND tenant_id = 1),
 NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user24@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 2차' AND tenant_id = 1),
 NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user25@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW());

-- =============================================
-- 28. 찜 데이터 (테넌트 1 기준 5개)
-- =============================================
INSERT INTO cm_wishlist_items (tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(1, (SELECT id FROM users WHERE email = 'user26@default.com'),
 (SELECT id FROM course_times WHERE title = 'Spring Boot 기초 2차' AND tenant_id = 1),
 NOW() - INTERVAL 5 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user27@default.com'),
 (SELECT id FROM course_times WHERE title = 'Java 프로그래밍 마스터 1차' AND tenant_id = 1),
 NOW() - INTERVAL 4 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user28@default.com'),
 (SELECT id FROM course_times WHERE title = 'Kubernetes 운영 실무 1차' AND tenant_id = 1),
 NOW() - INTERVAL 3 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user29@default.com'),
 (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1),
 NOW() - INTERVAL 2 DAY, NOW()),
(1, (SELECT id FROM users WHERE email = 'user30@default.com'),
 (SELECT id FROM course_times WHERE title = 'AWS 클라우드 아키텍처 2차' AND tenant_id = 1),
 NOW() - INTERVAL 1 DAY, NOW());

-- =============================================
-- 29. 테넌트 2, 3 추가 수강 데이터
-- =============================================
-- 테넌트 2 수강 데이터
INSERT INTO sis_enrollments (tenant_id, user_id, course_time_id, enrolled_at, type, status, progress_percent, score, completed_at, enrolled_by, created_at, updated_at, version) VALUES
-- 영업 기초 이론 1차 수강생
(2, (SELECT id FROM users WHERE email = 'user1@company-a.com'),
 (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2),
 NOW() - INTERVAL 10 DAY, 'MANDATORY', 'ENROLLED', 65, NULL, NULL,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 10 DAY, NOW(), 0),
(2, (SELECT id FROM users WHERE email = 'user2@company-a.com'),
 (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2),
 NOW() - INTERVAL 8 DAY, 'MANDATORY', 'ENROLLED', 45, NULL, NULL,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 8 DAY, NOW(), 0),
(2, (SELECT id FROM users WHERE email = 'user3@company-a.com'),
 (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2),
 NOW() - INTERVAL 12 DAY, 'MANDATORY', 'ENROLLED', 80, NULL, NULL,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 12 DAY, NOW(), 0),
-- 정보보안 기초 수강생 (필수 교육)
(2, (SELECT id FROM users WHERE email = 'user4@company-a.com'),
 (SELECT id FROM course_times WHERE title = '정보보안 기초 2026년' AND tenant_id = 2),
 NOW() - INTERVAL 5 DAY, 'MANDATORY', 'COMPLETED', 100, 100, NOW() - INTERVAL 2 DAY,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 5 DAY, NOW(), 0),
(2, (SELECT id FROM users WHERE email = 'user5@company-a.com'),
 (SELECT id FROM course_times WHERE title = '정보보안 기초 2026년' AND tenant_id = 2),
 NOW() - INTERVAL 6 DAY, 'MANDATORY', 'COMPLETED', 100, 100, NOW() - INTERVAL 3 DAY,
 (SELECT id FROM users WHERE email = 'operator1@company-a.com'), NOW() - INTERVAL 6 DAY, NOW(), 0);

-- 테넌트 3 수강 데이터
INSERT INTO sis_enrollments (tenant_id, user_id, course_time_id, enrolled_at, type, status, progress_percent, score, completed_at, enrolled_by, created_at, updated_at, version) VALUES
-- 스타트업 101 1차 수강생
(3, (SELECT id FROM users WHERE email = 'user1@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 10 DAY, 'VOLUNTARY', 'ENROLLED', 70, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user1@company-b.com'), NOW() - INTERVAL 10 DAY, NOW(), 0),
(3, (SELECT id FROM users WHERE email = 'user2@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 8 DAY, 'VOLUNTARY', 'ENROLLED', 55, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user2@company-b.com'), NOW() - INTERVAL 8 DAY, NOW(), 0),
(3, (SELECT id FROM users WHERE email = 'user3@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 12 DAY, 'VOLUNTARY', 'ENROLLED', 90, NULL, NULL,
 (SELECT id FROM users WHERE email = 'user3@company-b.com'), NOW() - INTERVAL 12 DAY, NOW(), 0),
-- IR 피칭 전략 1차 수료생
(3, (SELECT id FROM users WHERE email = 'user4@company-b.com'),
 (SELECT id FROM course_times WHERE title = 'IR 피칭 전략 1차' AND tenant_id = 3),
 NOW() - INTERVAL 60 DAY, 'VOLUNTARY', 'COMPLETED', 100, 92, NOW() - INTERVAL 35 DAY,
 (SELECT id FROM users WHERE email = 'user4@company-b.com'), NOW() - INTERVAL 60 DAY, NOW(), 0),
(3, (SELECT id FROM users WHERE email = 'user5@company-b.com'),
 (SELECT id FROM course_times WHERE title = 'IR 피칭 전략 1차' AND tenant_id = 3),
 NOW() - INTERVAL 55 DAY, 'VOLUNTARY', 'COMPLETED', 100, 88, NOW() - INTERVAL 32 DAY,
 (SELECT id FROM users WHERE email = 'user5@company-b.com'), NOW() - INTERVAL 55 DAY, NOW(), 0);

-- =============================================
-- 30. 테넌트 2, 3 로드맵 데이터
-- =============================================
-- 테넌트 2 로드맵: 신입사원 성장 로드맵
INSERT INTO roadmaps (tenant_id, title, description, author_id, status, enrolled_students, created_at, updated_at)
SELECT 2, '신입사원 필수 교육 로드맵',
       'A사 신입사원이 반드시 이수해야 하는 교육 과정입니다.',
       (SELECT id FROM users WHERE email = 'admin@company-a.com'), 'PUBLISHED', 20, NOW() - INTERVAL 30 DAY, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2);

-- [DISABLED] Program 엔티티 제거로 인해 주석 처리
/*
INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2),
       (SELECT id FROM cm_courses WHERE title = '신입사원 OJT 과정' AND tenant_id = 2), 0
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2)
AND program_id = (SELECT id FROM cm_courses WHERE title = '신입사원 OJT 과정' AND tenant_id = 2));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2),
       (SELECT id FROM cm_courses WHERE title = '정보보안 기초 과정' AND tenant_id = 2), 1
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2)
AND program_id = (SELECT id FROM cm_courses WHERE title = '정보보안 기초 과정' AND tenant_id = 2));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2),
       (SELECT id FROM cm_courses WHERE title = '개인정보보호법 과정' AND tenant_id = 2), 2
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '신입사원 필수 교육 로드맵' AND tenant_id = 2)
AND program_id = (SELECT id FROM cm_courses WHERE title = '개인정보보호법 과정' AND tenant_id = 2));
*/

-- 테넌트 3 로드맵: 창업가 성장 로드맵
INSERT INTO roadmaps (tenant_id, title, description, author_id, status, enrolled_students, created_at, updated_at)
SELECT 3, '예비 창업가 성장 로드맵',
       'B사 아카데미에서 제공하는 창업 준비 종합 교육 과정입니다.',
       (SELECT id FROM users WHERE email = 'admin@company-b.com'), 'PUBLISHED', 15, NOW() - INTERVAL 25 DAY, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3);

-- [DISABLED] Program 엔티티 제거로 인해 주석 처리
/*
INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3),
       (SELECT id FROM cm_courses WHERE title = '스타트업 101 과정' AND tenant_id = 3), 0
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3)
AND program_id = (SELECT id FROM cm_courses WHERE title = '스타트업 101 과정' AND tenant_id = 3));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3),
       (SELECT id FROM cm_courses WHERE title = '린 스타트업 방법론 과정' AND tenant_id = 3), 1
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3)
AND program_id = (SELECT id FROM cm_courses WHERE title = '린 스타트업 방법론 과정' AND tenant_id = 3));

INSERT INTO roadmap_programs (roadmap_id, program_id, order_index)
SELECT (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3),
       (SELECT id FROM cm_courses WHERE title = 'IR 피칭 전략 과정' AND tenant_id = 3), 2
WHERE NOT EXISTS (SELECT 1 FROM roadmap_programs WHERE roadmap_id = (SELECT id FROM roadmaps WHERE title = '예비 창업가 성장 로드맵' AND tenant_id = 3)
AND program_id = (SELECT id FROM cm_courses WHERE title = 'IR 피칭 전략 과정' AND tenant_id = 3));
*/

-- =============================================
-- 31. 커뮤니티 댓글 데이터 (테넌트별)
-- =============================================
-- 테넌트 1 댓글 (개발 관련 게시글)
INSERT INTO community_comments (tenant_id, post_id, author_id, content, parent_id, created_at, updated_at) VALUES
-- Spring Boot 시작하기 질문 댓글
(1, (SELECT id FROM community_posts WHERE title = 'Spring Boot 시작하기 질문' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user2@default.com'),
 'spring-boot-starter-web, spring-boot-starter-data-jpa 부터 시작하시면 좋아요!', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Spring Boot 시작하기 질문' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user3@default.com'),
 'Lombok도 추가하면 생산성이 올라갑니다.', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Spring Boot 시작하기 질문' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user1@default.com'),
 '답변 감사합니다! 바로 적용해볼게요.', NULL, NOW() - INTERVAL 7 DAY, NOW()),

-- JPA N+1 문제 댓글
(1, (SELECT id FROM community_posts WHERE title = 'JPA N+1 문제 해결 방법' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user4@default.com'),
 '@EntityGraph 어노테이션도 좋은 방법입니다.', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'JPA N+1 문제 해결 방법' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user5@default.com'),
 'Batch Size 설정으로 해결한 경험이 있어요.', NULL, NOW() - INTERVAL 7 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'JPA N+1 문제 해결 방법' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user6@default.com'),
 'QueryDSL 사용하시면 더 세밀하게 제어 가능합니다.', NULL, NOW() - INTERVAL 6 DAY, NOW()),

-- React Hooks 팁 댓글
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user7@default.com'),
 '정말 유용한 정보 감사합니다!', NULL, NOW() - INTERVAL 7 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user8@default.com'),
 'useCallback 남용하면 오히려 성능이 떨어질 수 있다고 들었는데 맞나요?', NULL, NOW() - INTERVAL 6 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user3@default.com'),
 '맞아요, 실제로 리렌더링이 문제가 될 때만 사용하는 게 좋습니다.', NULL, NOW() - INTERVAL 5 DAY, NOW()),

-- SQL 쿼리 최적화 댓글
(1, (SELECT id FROM community_posts WHERE title = 'SQL 쿼리 최적화 사례' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user9@default.com'),
 '인덱스 설계가 중요한 것 같네요.', NULL, NOW() - INTERVAL 4 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'SQL 쿼리 최적화 사례' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user10@default.com'),
 'EXPLAIN ANALYZE 결과도 공유해주실 수 있나요?', NULL, NOW() - INTERVAL 3 DAY, NOW()),

-- Clean Architecture 도입기 댓글
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user11@default.com'),
 '팀 전체가 동의해야 도입 가능하더라구요.', NULL, NOW() - INTERVAL 14 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user12@default.com'),
 '레이어 분리 기준이 궁금합니다.', NULL, NOW() - INTERVAL 13 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user13@default.com'),
 '도메인 레이어를 가장 안쪽에 두고, 외부 의존성은 어댑터로 분리했습니다.', NULL, NOW() - INTERVAL 12 DAY, NOW());

-- 테넌트 2 댓글 (기업 교육 관련)
INSERT INTO community_comments (tenant_id, post_id, author_id, content, parent_id, created_at, updated_at) VALUES
-- 영업 미팅 준비 팁 댓글
(2, (SELECT id FROM community_posts WHERE title = '영업 미팅 준비 팁' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user2@company-a.com'),
 '체크리스트 덕분에 미팅 성공적으로 마쳤습니다!', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '영업 미팅 준비 팁' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user3@company-a.com'),
 '고객 회사 뉴스도 미리 확인하면 좋아요.', NULL, NOW() - INTERVAL 8 DAY, NOW()),

-- 신입사원 적응 질문 댓글
(2, (SELECT id FROM community_posts WHERE title = '신입사원 적응 질문' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user4@company-a.com'),
 '먼저 팀원들 이름과 얼굴 외우기부터 하세요!', NULL, NOW() - INTERVAL 7 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '신입사원 적응 질문' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user5@company-a.com'),
 '회사 시스템 사용법 익히는 게 우선이에요.', NULL, NOW() - INTERVAL 6 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '신입사원 적응 질문' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user6@company-a.com'),
 '점심시간에 선배들과 이야기 나누면 적응이 빨라요.', NULL, NOW() - INTERVAL 5 DAY, NOW()),

-- 엑셀 단축키 댓글
(2, (SELECT id FROM community_posts WHERE title = '엑셀 단축키 모음' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user7@company-a.com'),
 'Ctrl+Shift+L 필터 단축키가 제일 많이 쓰여요!', NULL, NOW() - INTERVAL 10 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '엑셀 단축키 모음' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user8@company-a.com'),
 'Alt+= 자동합계 단축키 추가해주세요!', NULL, NOW() - INTERVAL 9 DAY, NOW()),

-- 연봉 협상 후기 댓글
(2, (SELECT id FROM community_posts WHERE title = '연봉 협상 후기' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user9@company-a.com'),
 '시장 조사 먼저 하는 게 중요하네요.', NULL, NOW() - INTERVAL 12 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '연봉 협상 후기' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user10@company-a.com'),
 '자신의 성과를 수치로 정리해가면 좋아요.', NULL, NOW() - INTERVAL 11 DAY, NOW());

-- 테넌트 3 댓글 (창업 관련)
INSERT INTO community_comments (tenant_id, post_id, author_id, content, parent_id, created_at, updated_at) VALUES
-- 스타트업 첫 투자 유치 후기 댓글
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user2@company-b.com'),
 'IR 자료 템플릿 공유 가능할까요?', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user3@company-b.com'),
 '어떤 VC에서 투자 받으셨나요?', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user1@company-b.com'),
 '프라이머에서 시드 투자 받았습니다!', NULL, NOW() - INTERVAL 7 DAY, NOW()),

-- PMF 찾기 실패담 댓글
(3, (SELECT id FROM community_posts WHERE title = 'PMF 찾기 실패담' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user4@company-b.com'),
 '실패 경험 공유 감사합니다. 많이 배웠어요.', NULL, NOW() - INTERVAL 6 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = 'PMF 찾기 실패담' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user5@company-b.com'),
 '고객 인터뷰를 더 많이 했어야 했네요.', NULL, NOW() - INTERVAL 5 DAY, NOW()),

-- 피칭 덱 작성 가이드 댓글
(3, (SELECT id FROM community_posts WHERE title = '피칭 덱 작성 가이드' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user6@company-b.com'),
 '10페이지가 적당한 것 같아요.', NULL, NOW() - INTERVAL 11 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '피칭 덱 작성 가이드' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user7@company-b.com'),
 'Problem-Solution-Market 순서가 좋더라구요.', NULL, NOW() - INTERVAL 10 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '피칭 덱 작성 가이드' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user8@company-b.com'),
 '숫자로 보여주는 게 중요합니다!', NULL, NOW() - INTERVAL 9 DAY, NOW()),

-- 스타트업 실패 원인 댓글
(3, (SELECT id FROM community_posts WHERE title = '스타트업 실패 원인' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user9@company-b.com'),
 '정말 솔직한 글이네요. 용기있는 공유 감사합니다.', NULL, NOW() - INTERVAL 2 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 실패 원인' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user10@company-b.com'),
 '저도 비슷한 경험이 있어서 공감됩니다.', NULL, NOW() - INTERVAL 1 DAY, NOW());

-- =============================================
-- 32. 커뮤니티 게시글 좋아요 데이터 (테넌트별)
-- =============================================
-- 테넌트 1 게시글 좋아요
INSERT INTO community_post_likes (tenant_id, post_id, user_id, created_at, updated_at) VALUES
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user1@default.com'), NOW() - INTERVAL 7 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user2@default.com'), NOW() - INTERVAL 6 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user4@default.com'), NOW() - INTERVAL 5 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'React Hooks 사용 팁 공유' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user5@default.com'), NOW() - INTERVAL 4 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'SQL 쿼리 최적화 사례' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user1@default.com'), NOW() - INTERVAL 4 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'SQL 쿼리 최적화 사례' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user3@default.com'), NOW() - INTERVAL 3 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'SQL 쿼리 최적화 사례' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user6@default.com'), NOW() - INTERVAL 2 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user2@default.com'), NOW() - INTERVAL 14 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user7@default.com'), NOW() - INTERVAL 13 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'Clean Architecture 도입기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user8@default.com'), NOW() - INTERVAL 12 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'MSA 도입 실패 경험' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user1@default.com'), NOW() - INTERVAL 12 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'MSA 도입 실패 경험' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user4@default.com'), NOW() - INTERVAL 11 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = 'MSA 도입 실패 경험' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user9@default.com'), NOW() - INTERVAL 10 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = '신입 개발자 취업 후기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user2@default.com'), NOW() - INTERVAL 19 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = '신입 개발자 취업 후기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user3@default.com'), NOW() - INTERVAL 18 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = '신입 개발자 취업 후기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user5@default.com'), NOW() - INTERVAL 17 DAY, NOW()),
(1, (SELECT id FROM community_posts WHERE title = '신입 개발자 취업 후기' AND tenant_id = 1),
 (SELECT id FROM users WHERE email = 'user10@default.com'), NOW() - INTERVAL 16 DAY, NOW());

-- 테넌트 2 게시글 좋아요
INSERT INTO community_post_likes (tenant_id, post_id, user_id, created_at, updated_at) VALUES
(2, (SELECT id FROM community_posts WHERE title = '영업 미팅 준비 팁' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user1@company-a.com'), NOW() - INTERVAL 9 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '영업 미팅 준비 팁' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user2@company-a.com'), NOW() - INTERVAL 8 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '보고서 작성 템플릿' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user3@company-a.com'), NOW() - INTERVAL 5 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '보고서 작성 템플릿' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user4@company-a.com'), NOW() - INTERVAL 4 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '보고서 작성 템플릿' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user5@company-a.com'), NOW() - INTERVAL 3 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '엑셀 단축키 모음' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user1@company-a.com'), NOW() - INTERVAL 10 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '엑셀 단축키 모음' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user6@company-a.com'), NOW() - INTERVAL 9 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '엑셀 단축키 모음' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user7@company-a.com'), NOW() - INTERVAL 8 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '사내 정치 생존기' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user2@company-a.com'), NOW() - INTERVAL 17 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '사내 정치 생존기' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user8@company-a.com'), NOW() - INTERVAL 16 DAY, NOW()),
(2, (SELECT id FROM community_posts WHERE title = '사내 정치 생존기' AND tenant_id = 2),
 (SELECT id FROM users WHERE email = 'user9@company-a.com'), NOW() - INTERVAL 15 DAY, NOW());

-- 테넌트 3 게시글 좋아요
INSERT INTO community_post_likes (tenant_id, post_id, user_id, created_at, updated_at) VALUES
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user2@company-b.com'), NOW() - INTERVAL 9 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user3@company-b.com'), NOW() - INTERVAL 8 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 첫 투자 유치 후기' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user4@company-b.com'), NOW() - INTERVAL 7 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = 'PMF 찾기 실패담' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user1@company-b.com'), NOW() - INTERVAL 6 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = 'PMF 찾기 실패담' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user5@company-b.com'), NOW() - INTERVAL 5 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 실패 원인' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user1@company-b.com'), NOW() - INTERVAL 2 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 실패 원인' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user2@company-b.com'), NOW() - INTERVAL 1 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '스타트업 실패 원인' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user6@company-b.com'), NOW() - INTERVAL 1 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '시리즈 A 준비 체크리스트' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user3@company-b.com'), NOW() - INTERVAL 18 DAY, NOW()),
(3, (SELECT id FROM community_posts WHERE title = '시리즈 A 준비 체크리스트' AND tenant_id = 3),
 (SELECT id FROM users WHERE email = 'user7@company-b.com'), NOW() - INTERVAL 17 DAY, NOW());

-- =============================================
-- 33. 커뮤니티 댓글 좋아요 데이터 (테넌트별)
-- =============================================
-- 테넌트 1 댓글 좋아요
INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 1, c.id, (SELECT id FROM users WHERE email = 'user4@default.com'), NOW() - INTERVAL 6 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 1 AND c.content LIKE '%spring-boot-starter%' LIMIT 1;

INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 1, c.id, (SELECT id FROM users WHERE email = 'user5@default.com'), NOW() - INTERVAL 5 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 1 AND c.content LIKE '%spring-boot-starter%' LIMIT 1;

INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 1, c.id, (SELECT id FROM users WHERE email = 'user6@default.com'), NOW() - INTERVAL 7 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 1 AND c.content LIKE '%@EntityGraph%' LIMIT 1;

INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 1, c.id, (SELECT id FROM users WHERE email = 'user7@default.com'), NOW() - INTERVAL 6 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 1 AND c.content LIKE '%QueryDSL%' LIMIT 1;

-- 테넌트 2 댓글 좋아요
INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 2, c.id, (SELECT id FROM users WHERE email = 'user1@company-a.com'), NOW() - INTERVAL 8 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 2 AND c.content LIKE '%체크리스트%' LIMIT 1;

INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 2, c.id, (SELECT id FROM users WHERE email = 'user2@company-a.com'), NOW() - INTERVAL 9 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 2 AND c.content LIKE '%Ctrl+Shift+L%' LIMIT 1;

-- 테넌트 3 댓글 좋아요
INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 3, c.id, (SELECT id FROM users WHERE email = 'user1@company-b.com'), NOW() - INTERVAL 8 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 3 AND c.content LIKE '%IR 자료%' LIMIT 1;

INSERT INTO community_comment_likes (tenant_id, comment_id, user_id, created_at, updated_at)
SELECT 3, c.id, (SELECT id FROM users WHERE email = 'user4@company-b.com'), NOW() - INTERVAL 7 DAY, NOW()
FROM community_comments c
WHERE c.tenant_id = 3 AND c.content LIKE '%프라이머%' LIMIT 1;

-- =============================================
-- 34. 추가 찜 데이터 (테넌트 2, 3)
-- =============================================
-- 테넌트 2 찜
INSERT INTO cm_wishlist_items (tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(2, (SELECT id FROM users WHERE email = 'user11@company-a.com'),
 (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2),
 NOW() - INTERVAL 5 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user12@company-a.com'),
 (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2),
 NOW() - INTERVAL 4 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user13@company-a.com'),
 (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2),
 NOW() - INTERVAL 3 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user14@company-a.com'),
 (SELECT id FROM course_times WHERE title = '정보보안 기초 2026년' AND tenant_id = 2),
 NOW() - INTERVAL 2 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user15@company-a.com'),
 (SELECT id FROM course_times WHERE title = '신입사원 OJT 2026년 1분기' AND tenant_id = 2),
 NOW() - INTERVAL 1 DAY, NOW());

-- 테넌트 3 찜
INSERT INTO cm_wishlist_items (tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(3, (SELECT id FROM users WHERE email = 'user11@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 5 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user12@company-b.com'),
 (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3),
 NOW() - INTERVAL 4 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user13@company-b.com'),
 (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3),
 NOW() - INTERVAL 3 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user14@company-b.com'),
 (SELECT id FROM course_times WHERE title = '그로스해킹 기초 1차' AND tenant_id = 3),
 NOW() - INTERVAL 2 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user15@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 1 DAY, NOW());

-- =============================================
-- 35. 추가 장바구니 데이터 (테넌트 2, 3)
-- =============================================
-- 테넌트 2 장바구니
INSERT INTO cart_items (tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(2, (SELECT id FROM users WHERE email = 'user16@company-a.com'),
 (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2),
 NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user17@company-a.com'),
 (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2),
 NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user18@company-a.com'),
 (SELECT id FROM course_times WHERE title = '영업 기초 이론 1차' AND tenant_id = 2),
 NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user19@company-a.com'),
 (SELECT id FROM course_times WHERE title = '정보보안 기초 2026년' AND tenant_id = 2),
 NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, NOW()),
(2, (SELECT id FROM users WHERE email = 'user20@company-a.com'),
 (SELECT id FROM course_times WHERE title = '신입사원 OJT 2026년 1분기' AND tenant_id = 2),
 NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW());

-- 테넌트 3 장바구니
INSERT INTO cart_items (tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(3, (SELECT id FROM users WHERE email = 'user16@company-b.com'),
 (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3),
 NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user17@company-b.com'),
 (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3),
 NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user18@company-b.com'),
 (SELECT id FROM course_times WHERE title = '그로스해킹 기초 1차' AND tenant_id = 3),
 NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user19@company-b.com'),
 (SELECT id FROM course_times WHERE title = '스타트업 101 1차' AND tenant_id = 3),
 NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, NOW()),
(3, (SELECT id FROM users WHERE email = 'user20@company-b.com'),
 (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3),
 NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW());

-- =============================================
-- 36. 추가 리뷰/수강평 데이터 (다양한 차수)
-- =============================================
-- 테넌트 1: React & TypeScript 실전 1차 리뷰
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(1, (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user21@default.com'), 5, 'TypeScript와 React 조합의 장점을 제대로 배웠습니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user22@default.com'), 4, '실전 예제가 많아서 좋았어요.', 85, NOW() - INTERVAL 4 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user23@default.com'), 5, '타입 안정성의 중요성을 깨달았습니다.', 90, NOW() - INTERVAL 3 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user24@default.com'), 4, 'Hooks와 TypeScript 조합이 어렵지만 익숙해지니 좋네요.', 75, NOW() - INTERVAL 2 DAY, NOW(), 0),
(1, (SELECT id FROM course_times WHERE title = 'React & TypeScript 실전 1차' AND tenant_id = 1), (SELECT id FROM users WHERE email = 'user25@default.com'), 5, '프론트엔드 개발자 필수 강의!', 95, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- 테넌트 2: 팀 리더십 기초 1차 리뷰
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(2, (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user11@company-a.com'), 5, '리더십의 기본을 배울 수 있었습니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user12@company-a.com'), 4, '팀원 동기부여 방법이 유익했어요.', 80, NOW() - INTERVAL 4 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user13@company-a.com'), 5, '실제 사례 기반의 강의라서 좋았습니다.', 90, NOW() - INTERVAL 3 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user14@company-a.com'), 4, '코칭 기술을 배울 수 있어서 좋았어요.', 70, NOW() - INTERVAL 2 DAY, NOW(), 0),
(2, (SELECT id FROM course_times WHERE title = '팀 리더십 기초 1차' AND tenant_id = 2), (SELECT id FROM users WHERE email = 'user15@company-a.com'), 5, '팀장 준비하는 분들께 추천합니다!', 85, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- 테넌트 3: 디지털 마케팅 기초 1차 리뷰
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(3, (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user11@company-b.com'), 5, '마케팅 입문자에게 최고의 강의입니다.', 100, NOW() - INTERVAL 5 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user12@company-b.com'), 4, 'SNS 마케팅 파트가 특히 좋았어요.', 85, NOW() - INTERVAL 4 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user13@company-b.com'), 5, '실전에 바로 적용할 수 있는 내용이 많아요.', 90, NOW() - INTERVAL 3 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user14@company-b.com'), 4, 'SEO 기초를 이해하게 되었습니다.', 75, NOW() - INTERVAL 2 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '디지털 마케팅 기초 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user15@company-b.com'), 5, '스타트업 마케터 필수 강의!', 95, NOW() - INTERVAL 1 DAY, NOW(), 0);

-- 테넌트 3: 프로덕트 매니지먼트 1차 리뷰
INSERT INTO cm_course_reviews (tenant_id, course_time_id, user_id, rating, content, completion_rate, created_at, updated_at, version) VALUES
(3, (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user16@company-b.com'), 5, 'PM 역할에 대해 명확히 이해하게 되었습니다.', 100, NOW() - INTERVAL 10 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user17@company-b.com'), 5, '사용자 리서치 방법론이 유익했어요.', 95, NOW() - INTERVAL 9 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user18@company-b.com'), 4, 'PRD 작성법을 배울 수 있어서 좋았습니다.', 80, NOW() - INTERVAL 8 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user19@company-b.com'), 5, 'A/B 테스트 설계 방법이 실용적이었어요.', 90, NOW() - INTERVAL 7 DAY, NOW(), 0),
(3, (SELECT id FROM course_times WHERE title = '프로덕트 매니지먼트 1차' AND tenant_id = 3), (SELECT id FROM users WHERE email = 'user20@company-b.com'), 4, '데이터 기반 의사결정의 중요성을 배웠습니다.', 85, NOW() - INTERVAL 6 DAY, NOW(), 0);

-- =============================================
-- 37. 콘텐츠(Content) 상세 데이터 (테넌트 1, 2, 3)
-- =============================================

-- ============================================
-- 테넌트 1: VIDEO 콘텐츠
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1001, 1, 0, 'ACTIVE', 1, 1, 'React 소개 영상', 'react-intro.mp4', 'react-intro-uuid.mp4', 'VIDEO', 52428800, 1200, '1920x1080', '/content/videos/react-intro-uuid.mp4', '/content/thumbnails/react-intro.jpg', 'React의 기본 개념과 특징을 소개합니다.', 'React,JavaScript,Frontend', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1001);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1002, 1, 0, 'ACTIVE', 1, 1, '개발환경 구성 가이드', 'dev-env-setup.mp4', 'dev-env-setup-uuid.mp4', 'VIDEO', 94371840, 1800, '1920x1080', '/content/videos/dev-env-setup-uuid.mp4', '/content/thumbnails/dev-env-setup.jpg', 'Node.js, npm, create-react-app 설치 및 설정 가이드', 'Node.js,npm,환경설정', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1002);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1003, 1, 0, 'ACTIVE', 1, 1, '함수형 컴포넌트 강의', 'func-component.mp4', 'func-component-uuid.mp4', 'VIDEO', 125829120, 2400, '1920x1080', '/content/videos/func-component-uuid.mp4', '/content/thumbnails/func-component.jpg', 'React 함수형 컴포넌트 작성법 상세 강의', 'React,Component,함수형', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1003);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1004, 1, 0, 'ACTIVE', 1, 1, 'Props와 State 이해하기', 'props-state.mp4', 'props-state-uuid.mp4', 'VIDEO', 110100480, 2100, '1920x1080', '/content/videos/props-state-uuid.mp4', '/content/thumbnails/props-state.jpg', 'Props와 State의 차이와 활용법', 'React,Props,State', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1004);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1005, 1, 0, 'ACTIVE', 1, 1, 'Spring Security 설정', 'spring-security.mp4', 'spring-security-uuid.mp4', 'VIDEO', 141557760, 2700, '1920x1080', '/content/videos/spring-security-uuid.mp4', '/content/thumbnails/spring-security.jpg', 'SecurityConfig 작성 및 기본 설정 강의', 'Spring,Security,Backend', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1005);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1006, 1, 0, 'ACTIVE', 1, 1, 'JWT 인증 구현하기', 'jwt-auth.mp4', 'jwt-auth-uuid.mp4', 'VIDEO', 157286400, 3000, '1920x1080', '/content/videos/jwt-auth-uuid.mp4', '/content/thumbnails/jwt-auth.jpg', 'JWT를 활용한 Stateless 인증 구현', 'JWT,Spring,Security', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1006);

-- AWS 강의 콘텐츠
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1007, 1, 0, 'ACTIVE', 1, 1, 'AWS EC2 시작하기', 'aws-ec2.mp4', 'aws-ec2-uuid.mp4', 'VIDEO', 136314880, 2600, '1920x1080', '/content/videos/aws-ec2-uuid.mp4', '/content/thumbnails/aws-ec2.jpg', 'EC2 인스턴스 생성 및 설정', 'AWS,EC2,Cloud', true, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1007);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1008, 1, 0, 'ACTIVE', 1, 1, 'S3 버킷 활용법', 's3-bucket.mp4', 's3-bucket-uuid.mp4', 'VIDEO', 115343360, 2200, '1920x1080', '/content/videos/s3-bucket-uuid.mp4', '/content/thumbnails/s3-bucket.jpg', 'S3 버킷 생성 및 파일 업로드', 'AWS,S3,Storage', true, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1008);

-- Python 데이터 분석 콘텐츠
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1009, 1, 0, 'ACTIVE', 1, 1, 'Pandas 기초', 'pandas-basic.mp4', 'pandas-basic-uuid.mp4', 'VIDEO', 120586240, 2300, '1920x1080', '/content/videos/pandas-basic-uuid.mp4', '/content/thumbnails/pandas-basic.jpg', 'Pandas DataFrame 기초 강의', 'Python,Pandas,Data', true, '데이터', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1009);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1010, 1, 0, 'ACTIVE', 1, 1, 'Matplotlib 시각화', 'matplotlib.mp4', 'matplotlib-uuid.mp4', 'VIDEO', 131072000, 2500, '1920x1080', '/content/videos/matplotlib-uuid.mp4', '/content/thumbnails/matplotlib.jpg', '데이터 시각화 기초', 'Python,Matplotlib,시각화', true, '데이터', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1010);

-- ============================================
-- 테넌트 1: DOCUMENT 콘텐츠
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1011, 1, 0, 'ACTIVE', 1, 1, 'React 핵심 요약 PDF', 'react-summary.pdf', 'react-summary-uuid.pdf', 'DOCUMENT', 2097152, 25, '/content/documents/react-summary-uuid.pdf', '/content/thumbnails/react-summary.jpg', 'React 핵심 개념 요약 문서', 'React,Summary,PDF', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1011);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1012, 1, 0, 'ACTIVE', 1, 1, 'Spring Boot 가이드', 'spring-guide.pdf', 'spring-guide-uuid.pdf', 'DOCUMENT', 3145728, 42, '/content/documents/spring-guide-uuid.pdf', '/content/thumbnails/spring-guide.jpg', 'Spring Boot 실무 가이드북', 'Spring,Guide,PDF', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1012);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1013, 1, 0, 'ACTIVE', 1, 1, 'AWS 아키텍처 다이어그램', 'aws-architecture.pptx', 'aws-architecture-uuid.pptx', 'DOCUMENT', 5242880, 35, '/content/documents/aws-architecture-uuid.pptx', '/content/thumbnails/aws-architecture.jpg', 'AWS 서비스 아키텍처 설계 자료', 'AWS,Architecture,PPT', true, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1013);

-- ============================================
-- 테넌트 1: EXTERNAL_LINK 콘텐츠
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, content_type, external_url, duration, description, tags, downloadable, category, created_at, updated_at)
SELECT 1014, 1, 0, 'ACTIVE', 1, 1, 'React 공식 문서', 'EXTERNAL_LINK', 'https://react.dev', NULL, 'React 공식 문서 링크', 'React,Documentation', false, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1014);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, content_type, external_url, duration, description, tags, downloadable, category, created_at, updated_at)
SELECT 1015, 1, 0, 'ACTIVE', 1, 1, 'Spring 공식 문서', 'EXTERNAL_LINK', 'https://spring.io/projects/spring-boot', NULL, 'Spring Boot 공식 문서', 'Spring,Documentation', false, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1015);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, content_type, external_url, duration, description, tags, downloadable, category, created_at, updated_at)
SELECT 1016, 1, 0, 'ACTIVE', 1, 1, 'AWS 공식 문서', 'EXTERNAL_LINK', 'https://docs.aws.amazon.com', NULL, 'AWS 공식 문서', 'AWS,Documentation', false, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1016);

-- ============================================
-- 테넌트 1: IMAGE 콘텐츠
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, resolution, file_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1017, 1, 0, 'ACTIVE', 1, 1, 'React 아키텍처 다이어그램', 'react-diagram.png', 'react-diagram-uuid.png', 'IMAGE', 524288, '1920x1080', '/content/images/react-diagram-uuid.png', 'React 컴포넌트 구조 다이어그램', 'React,Diagram,Architecture', true, '개발', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1017);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, resolution, file_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1018, 1, 0, 'ACTIVE', 1, 1, 'AWS 서비스 인포그래픽', 'aws-infographic.jpg', 'aws-infographic-uuid.jpg', 'IMAGE', 1048576, '2560x1440', '/content/images/aws-infographic-uuid.jpg', 'AWS 주요 서비스 인포그래픽', 'AWS,Infographic,Cloud', true, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1018);

-- ============================================
-- 테넌트 1: AUDIO 콘텐츠
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, file_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1019, 1, 0, 'ACTIVE', 1, 1, '개발자 성장 팟캐스트 EP.1', 'podcast-ep1.mp3', 'podcast-ep1-uuid.mp3', 'AUDIO', 31457280, 1800, '/content/audio/podcast-ep1-uuid.mp3', '주니어 개발자 성장기 팟캐스트', 'Podcast,Developer,Career', true, '일반', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1019);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, file_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 1020, 1, 0, 'ACTIVE', 1, 1, '클라우드 트렌드 팟캐스트', 'cloud-podcast.mp3', 'cloud-podcast-uuid.mp3', 'AUDIO', 26214400, 1500, '/content/audio/cloud-podcast-uuid.mp3', '2025년 클라우드 트렌드 분석', 'Podcast,Cloud,Trend', true, '클라우드', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 1020);

-- ============================================
-- 테넌트 2: VIDEO 콘텐츠 (A사 교육센터)
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2001, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), 1, '영업 기초 이론', 'sales-basic.mp4', 'sales-basic-uuid.mp4', 'VIDEO', 52428800, 1200, '1920x1080', '/content/videos/sales-basic-uuid.mp4', '/content/thumbnails/sales-basic.jpg', '영업의 기본 개념과 프로세스를 소개합니다.', '영업,Sales,기초', true, '영업교육', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2001);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2002, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), 1, '고객 상담 기법', 'customer-consulting.mp4', 'customer-consulting-uuid.mp4', 'VIDEO', 94371840, 1800, '1920x1080', '/content/videos/customer-consulting-uuid.mp4', '/content/thumbnails/customer-consulting.jpg', '효과적인 고객 상담 및 니즈 파악 기법', '상담,고객,커뮤니케이션', true, '영업교육', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2002);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2003, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), 1, '리더십 기초', 'leadership-basic.mp4', 'leadership-basic-uuid.mp4', 'VIDEO', 125829120, 2400, '1920x1080', '/content/videos/leadership-basic-uuid.mp4', '/content/thumbnails/leadership-basic.jpg', '리더십의 핵심 원리와 실천 방법', '리더십,Leadership,관리', true, '리더십', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2003);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2004, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), 1, '팀 빌딩 워크샵', 'team-building.mp4', 'team-building-uuid.mp4', 'VIDEO', 110100480, 2100, '1920x1080', '/content/videos/team-building-uuid.mp4', '/content/thumbnails/team-building.jpg', '효과적인 팀 구성과 협업 방법', '팀빌딩,협업,조직문화', true, '리더십', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2004);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2005, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-a.com'), 1, '정보보안 기초', 'security-basic.mp4', 'security-basic-uuid.mp4', 'VIDEO', 141557760, 2700, '1920x1080', '/content/videos/security-basic-uuid.mp4', '/content/thumbnails/security-basic.jpg', '기업 정보보안의 기본 개념과 실천', '보안,Security,컴플라이언스', true, '컴플라이언스', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2005);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2006, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-a.com'), 1, '신입사원 온보딩', 'onboarding.mp4', 'onboarding-uuid.mp4', 'VIDEO', 157286400, 3000, '1920x1080', '/content/videos/onboarding-uuid.mp4', '/content/thumbnails/onboarding.jpg', '신입사원을 위한 회사 소개 및 업무 가이드', '온보딩,신입,OJT', true, '신입교육', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2006);

-- 테넌트 2: DOCUMENT 콘텐츠
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2011, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-a.com'), 1, '영업 매뉴얼', 'sales-manual.pdf', 'sales-manual-uuid.pdf', 'DOCUMENT', 2097152, 50, '/content/documents/sales-manual-uuid.pdf', '/content/thumbnails/sales-manual.jpg', '영업 프로세스 및 가이드라인', '영업,매뉴얼,가이드', true, '영업교육', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2011);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 2012, 2, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-a.com'), 1, '보안 정책 문서', 'security-policy.pdf', 'security-policy-uuid.pdf', 'DOCUMENT', 1048576, 30, '/content/documents/security-policy-uuid.pdf', '/content/thumbnails/security-policy.jpg', '회사 정보보안 정책 및 규정', '보안,정책,규정', true, '컴플라이언스', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 2012);

-- ============================================
-- 테넌트 3: VIDEO 콘텐츠 (B사 아카데미)
-- ============================================
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3001, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), 1, '스타트업 101', 'startup-101.mp4', 'startup-101-uuid.mp4', 'VIDEO', 52428800, 1200, '1920x1080', '/content/videos/startup-101-uuid.mp4', '/content/thumbnails/startup-101.jpg', '스타트업 창업의 기본 개념과 프로세스', '스타트업,창업,기초', true, '스타트업기초', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3001);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3002, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), 1, '디지털 마케팅 기초', 'digital-marketing.mp4', 'digital-marketing-uuid.mp4', 'VIDEO', 94371840, 1800, '1920x1080', '/content/videos/digital-marketing-uuid.mp4', '/content/thumbnails/digital-marketing.jpg', 'SEO, SEM, SNS 마케팅 기초', '마케팅,디지털,SNS', true, '마케팅', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3002);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3003, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), 1, '프로덕트 매니지먼트', 'product-management.mp4', 'product-management-uuid.mp4', 'VIDEO', 125829120, 2400, '1920x1080', '/content/videos/product-management-uuid.mp4', '/content/thumbnails/product-management.jpg', 'PM의 역할과 프로덕트 개발 프로세스', 'PM,프로덕트,기획', true, '프로덕트', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3003);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3004, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), 1, '그로스해킹 전략', 'growth-hacking.mp4', 'growth-hacking-uuid.mp4', 'VIDEO', 110100480, 2100, '1920x1080', '/content/videos/growth-hacking-uuid.mp4', '/content/thumbnails/growth-hacking.jpg', '스타트업을 위한 그로스해킹 전략', '그로스,해킹,성장', true, '그로스해킹', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3004);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3005, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), 1, '투자유치 전략', 'funding-strategy.mp4', 'funding-strategy-uuid.mp4', 'VIDEO', 141557760, 2700, '1920x1080', '/content/videos/funding-strategy-uuid.mp4', '/content/thumbnails/funding-strategy.jpg', 'VC 투자유치를 위한 준비와 전략', '투자,VC,펀딩', true, '투자유치', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3005);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, duration, resolution, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3006, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), 1, '피칭 마스터클래스', 'pitching-master.mp4', 'pitching-master-uuid.mp4', 'VIDEO', 157286400, 3000, '1920x1080', '/content/videos/pitching-master-uuid.mp4', '/content/thumbnails/pitching-master.jpg', '성공적인 피칭을 위한 스토리텔링', '피칭,발표,스토리텔링', true, '투자유치', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3006);

-- 테넌트 3: DOCUMENT 콘텐츠
INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3011, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer1@company-b.com'), 1, '사업계획서 템플릿', 'business-plan-template.pdf', 'business-plan-template-uuid.pdf', 'DOCUMENT', 2097152, 25, '/content/documents/business-plan-template-uuid.pdf', '/content/thumbnails/business-plan.jpg', '스타트업 사업계획서 작성 템플릿', '사업계획서,템플릿,창업', true, '스타트업기초', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3011);

INSERT INTO content (id, tenant_id, version, status, created_by, current_version, original_file_name, uploaded_file_name, stored_file_name, content_type, file_size, page_count, file_path, thumbnail_path, description, tags, downloadable, category, created_at, updated_at)
SELECT 3012, 3, 0, 'ACTIVE', (SELECT id FROM users WHERE email = 'designer2@company-b.com'), 1, '투자유치 체크리스트', 'funding-checklist.pdf', 'funding-checklist-uuid.pdf', 'DOCUMENT', 1048576, 15, '/content/documents/funding-checklist-uuid.pdf', '/content/thumbnails/funding-checklist.jpg', '시리즈 A 투자유치 준비 체크리스트', '투자,체크리스트,준비', true, '투자유치', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM content WHERE id = 3012);

-- @Version 필드 NULL 수정
UPDATE content SET version = 0 WHERE version IS NULL;
UPDATE content SET current_version = 1 WHERE current_version IS NULL;
