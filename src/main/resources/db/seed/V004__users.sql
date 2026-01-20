-- =============================================
-- V004: 사용자 데이터 (명시적 ID, 멀티롤 지원)
-- =============================================
-- 비밀번호: 1q2w3e4r! (BCrypt 암호화)
-- ID 범위:
--   시스템 관리자: 1
--   테넌트 1 관리자급: 2-10
--   테넌트 1 일반 사용자: 101-200
--   테넌트 2 관리자급: 11-20
--   테넌트 2 일반 사용자: 1001-1100
--   테넌트 3 관리자급: 21-30
--   테넌트 3 일반 사용자: 2001-2100

-- ===== 시스템 관리자 (전체 1명) =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(1, NULL, 'sysadmin@mzc.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김태호(시스템 관리자)', '010-0000-0001', NULL, NULL, 'SYSTEM_ADMIN', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 1 (기본 테넌트) 관리자/운영자/설계자 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
-- 테넌트 관리자
(2, 1, 'admin@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이정훈(테넌트 관리자)', '010-1000-0001', '경영지원팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
-- 운영자
(3, 1, 'operator1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박성민(운영자)', '010-1000-0002', '경영지원팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
(4, 1, 'operator2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '최유진(운영자)', '010-1000-0003', '경영지원팀', '대리', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
-- 강의 개설자 (설계자/디자이너)
(5, 1, 'designer1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '정동욱(강의 개설자)', '010-1000-0004', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(6, 1, 'designer2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '한소영(강의 개설자)', '010-1000-0005', '개발팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(7, 1, 'designer3@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '오민지(강의 개설자)', '010-1000-0006', '디자인팀', '대리', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
-- 일반 사용자 (테스트용)
(8, 1, 'creator@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '송재현(강의 개설자)', '010-1000-0007', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
-- 멀티롤 테스트 사용자 (운영자 + 설계자)
(9, 1, 'multi1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강현우(멀티롤)', '010-1000-0010', '개발팀', '차장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
-- 멀티롤 테스트 사용자 (설계자 + 강사)
(10, 1, 'multi2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '윤서희(멀티롤)', '010-1000-0011', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 1 일반 사용자 50명 (user1 ~ user50) - 부서/직급 분배 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
-- 개발팀 (15명)
(101, 1, 'user1@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김민준', '010-1001-0001', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(102, 1, 'user2@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이서연', '010-1001-0002', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(103, 1, 'user3@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박지호', '010-1001-0003', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(104, 1, 'user4@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '최수아', '010-1001-0004', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(105, 1, 'user5@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '정우진', '010-1001-0005', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(106, 1, 'user6@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강하은', '010-1001-0006', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(107, 1, 'user7@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '윤준서', '010-1001-0007', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(108, 1, 'user8@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '장예은', '010-1001-0008', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(109, 1, 'user9@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '임도현', '010-1001-0009', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(110, 1, 'user10@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '한소희', '010-1001-0010', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(111, 1, 'user11@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '오시우', '010-1001-0011', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(112, 1, 'user12@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '서유진', '010-1001-0012', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(113, 1, 'user13@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '신현우', '010-1001-0013', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(114, 1, 'user14@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '권민서', '010-1001-0014', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(115, 1, 'user15@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '황지안', '010-1001-0015', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 마케팅팀 (10명)
(116, 1, 'user16@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '안서준', '010-1001-0016', '마케팅팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(117, 1, 'user17@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '송예린', '010-1001-0017', '마케팅팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(118, 1, 'user18@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '전민재', '010-1001-0018', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(119, 1, 'user19@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '홍수빈', '010-1001-0019', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(120, 1, 'user20@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '유하준', '010-1001-0020', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(121, 1, 'user21@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '조아린', '010-1001-0021', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(122, 1, 'user22@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '백승민', '010-1001-0022', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(123, 1, 'user23@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '노지원', '010-1001-0023', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(124, 1, 'user24@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '하태민', '010-1001-0024', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(125, 1, 'user25@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '문채원', '010-1001-0025', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 인사팀 (8명)
(126, 1, 'user26@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '양현준', '010-1001-0026', '인사팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(127, 1, 'user27@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '배나은', '010-1001-0027', '인사팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(128, 1, 'user28@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '남지훈', '010-1001-0028', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(129, 1, 'user29@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '심유나', '010-1001-0029', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(130, 1, 'user30@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '곽동현', '010-1001-0030', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(131, 1, 'user31@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '탁서영', '010-1001-0031', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(132, 1, 'user32@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '엄재윤', '010-1001-0032', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(133, 1, 'user33@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '표다인', '010-1001-0033', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 영업팀 (10명)
(134, 1, 'user34@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '추성훈', '010-1001-0034', '영업팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(135, 1, 'user35@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '방지민', '010-1001-0035', '영업팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(136, 1, 'user36@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '피승현', '010-1001-0036', '영업팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(137, 1, 'user37@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '공서아', '010-1001-0037', '영업팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(138, 1, 'user38@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '감우빈', '010-1001-0038', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(139, 1, 'user39@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '옥지아', '010-1001-0039', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(140, 1, 'user40@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '석현서', '010-1001-0040', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(141, 1, 'user41@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '빈예원', '010-1001-0041', '영업팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(142, 1, 'user42@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '탕준호', '010-1001-0042', '영업팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(143, 1, 'user43@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '채소윤', '010-1001-0043', '영업팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
-- 디자인팀 (5명)
(144, 1, 'user44@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '두시현', '010-1001-0044', '디자인팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(145, 1, 'user45@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '라하린', '010-1001-0045', '디자인팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(146, 1, 'user46@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '마건우', '010-1001-0046', '디자인팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(147, 1, 'user47@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '바윤서', '010-1001-0047', '디자인팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(148, 1, 'user48@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '사지후', '010-1001-0048', '디자인팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
-- 경영지원팀 (2명)
(149, 1, 'user49@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '아채아', '010-1001-0049', '경영지원팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(150, 1, 'user50@default.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '자은우', '010-1001-0050', '경영지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 2 (삼성전자) 관리자급 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(11, 2, 'admin@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '장민혁(테넌트 관리자)', '010-2000-0001', '인사팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
(12, 2, 'operator1@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김세진(운영자)', '010-2000-0002', '인사팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
(13, 2, 'designer1@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이태영(강의 개설자)', '010-2000-0003', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(14, 2, 'creator@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박준혁(강의 개설자)', '010-2000-0004', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 2 일반 사용자 30명 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(1001, 2, 'user1@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이민수', '010-2001-0001', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1002, 2, 'user2@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김영희', '010-2001-0002', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1003, 2, 'user3@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박철수', '010-2001-0003', '기획팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1004, 2, 'user4@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '최지영', '010-2001-0004', '기획팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1005, 2, 'user5@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '정현우', '010-2001-0005', '품질관리팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1006, 2, 'user6@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강서연', '010-2001-0006', '품질관리팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1007, 2, 'user7@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '윤재민', '010-2001-0007', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1008, 2, 'user8@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '장하늘', '010-2001-0008', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1009, 2, 'user9@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '임소라', '010-2001-0009', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1010, 2, 'user10@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '한동욱', '010-2001-0010', '기획팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1011, 2, 'user11@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '오정민', '010-2001-0011', '재무팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(1012, 2, 'user12@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '서민정', '010-2001-0012', '재무팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1013, 2, 'user13@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '신우진', '010-2001-0013', '재무팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1014, 2, 'user14@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '권나리', '010-2001-0014', '인사팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1015, 2, 'user15@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '황성민', '010-2001-0015', '인사팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1016, 2, 'user16@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '안지우', '010-2001-0016', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(1017, 2, 'user17@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '송태희', '010-2001-0017', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1018, 2, 'user18@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '전준혁', '010-2001-0018', '품질관리팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(1019, 2, 'user19@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '홍서아', '010-2001-0019', '품질관리팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1020, 2, 'user20@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '유민호', '010-2001-0020', '기획팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1021, 2, 'user21@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '조예진', '010-2001-0021', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1022, 2, 'user22@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '백승호', '010-2001-0022', '재무팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1023, 2, 'user23@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '노현서', '010-2001-0023', '인사팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(1024, 2, 'user24@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '하지원', '010-2001-0024', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(1025, 2, 'user25@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '문재현', '010-2001-0025', '기획팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1026, 2, 'user26@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '양서윤', '010-2001-0026', '품질관리팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1027, 2, 'user27@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '배현준', '010-2001-0027', '재무팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1028, 2, 'user28@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '남지현', '010-2001-0028', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1029, 2, 'user29@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '심도윤', '010-2001-0029', '인사팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(1030, 2, 'user30@samsung.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '곽하린', '010-2001-0030', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 3 (네이버) 관리자급 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(21, 3, 'admin@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '조영훈(테넌트 관리자)', '010-3000-0001', '운영팀', '부장', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()),
(22, 3, 'operator1@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '서지현(운영자)', '010-3000-0002', '운영팀', '과장', 'OPERATOR', 'ACTIVE', NOW(), NOW()),
(23, 3, 'designer1@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '나현수(강의 개설자)', '010-3000-0003', '개발팀', '과장', 'DESIGNER', 'ACTIVE', NOW(), NOW()),
(24, 3, 'creator@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '류승범(강의 개설자)', '010-3000-0004', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW());

-- ===== 테넌트 3 일반 사용자 30명 =====
INSERT INTO users (id, tenant_id, email, password, name, phone, department, position, role, status, created_at, updated_at) VALUES
(2001, 3, 'user1@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '김도현', '010-3001-0001', '개발팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2002, 3, 'user2@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '이수빈', '010-3001-0002', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2003, 3, 'user3@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '박재호', '010-3001-0003', '마케팅팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2004, 3, 'user4@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '최서현', '010-3001-0004', '마케팅팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2005, 3, 'user5@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '정민재', '010-3001-0005', '운영팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2006, 3, 'user6@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '강예진', '010-3001-0006', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2007, 3, 'user7@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '윤성호', '010-3001-0007', '고객지원팀', '과장', 'USER', 'ACTIVE', NOW(), NOW()),
(2008, 3, 'user8@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '장미래', '010-3001-0008', '고객지원팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2009, 3, 'user9@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '임서준', '010-3001-0009', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2010, 3, 'user10@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '한유나', '010-3001-0010', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2011, 3, 'user11@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '오재원', '010-3001-0011', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2012, 3, 'user12@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '서하은', '010-3001-0012', '개발팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2013, 3, 'user13@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '신우빈', '010-3001-0013', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2014, 3, 'user14@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '권지아', '010-3001-0014', '마케팅팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2015, 3, 'user15@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '황시현', '010-3001-0015', '운영팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2016, 3, 'user16@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '안지후', '010-3001-0016', '운영팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2017, 3, 'user17@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '송태영', '010-3001-0017', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2018, 3, 'user18@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '전서아', '010-3001-0018', '고객지원팀', '인턴', 'USER', 'ACTIVE', NOW(), NOW()),
(2019, 3, 'user19@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '홍현우', '010-3001-0019', '개발팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2020, 3, 'user20@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '유민서', '010-3001-0020', '마케팅팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2021, 3, 'user21@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '조예나', '010-3001-0021', '고객지원팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2022, 3, 'user22@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '백도윤', '010-3001-0022', '개발팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(2023, 3, 'user23@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '노하린', '010-3001-0023', '마케팅팀', '차장', 'USER', 'ACTIVE', NOW(), NOW()),
(2024, 3, 'user24@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '하준서', '010-3001-0024', '운영팀', '팀장', 'USER', 'ACTIVE', NOW(), NOW()),
(2025, 3, 'user25@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '문서연', '010-3001-0025', '개발팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2026, 3, 'user26@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '양지민', '010-3001-0026', '개발팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2027, 3, 'user27@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '배현서', '010-3001-0027', '마케팅팀', '사원', 'USER', 'ACTIVE', NOW(), NOW()),
(2028, 3, 'user28@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '남지우', '010-3001-0028', '운영팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2029, 3, 'user29@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '심채아', '010-3001-0029', '고객지원팀', '대리', 'USER', 'ACTIVE', NOW(), NOW()),
(2030, 3, 'user30@naver.com', '$2a$10$4hFhr508/iEYj4.XDJ4DQOf6nq.vW6eWbUP4NQFD0yUhV8sWHYQWa', '곽서준', '010-3001-0030', '고객지원팀', '사원', 'USER', 'ACTIVE', NOW(), NOW());
