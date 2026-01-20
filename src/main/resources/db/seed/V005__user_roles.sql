-- =============================================
-- V005: 사용자 역할 데이터 (멀티롤 지원)
-- =============================================
-- 각 사용자의 역할을 user_roles 테이블에 저장
-- 멀티롤 사용자는 여러 역할을 가질 수 있음
-- ID 범위:
--   테넌트 1 관리자급 역할: 1-20
--   테넌트 1 일반 사용자 역할: 101-155 (강사 복합롤 포함)
--   테넌트 2 역할: 1001-1037 (강사 복합롤 포함)
--   테넌트 3 역할: 2001-2037 (강사 복합롤 포함)

-- ===== 테넌트 1 관리자급 역할 =====
INSERT INTO user_roles (id, user_id, role, created_at) VALUES
-- 시스템 관리자
(1, 1, 'SYSTEM_ADMIN', NOW()),
-- 테넌트 관리자
(2, 2, 'TENANT_ADMIN', NOW()),
-- 운영자
(3, 3, 'OPERATOR', NOW()),
(4, 4, 'OPERATOR', NOW()),
-- 설계자
(5, 5, 'DESIGNER', NOW()),
(6, 6, 'DESIGNER', NOW()),
(7, 7, 'DESIGNER', NOW()),
-- 강의 개설자 (일반 사용자 역할)
(8, 8, 'USER', NOW()),
-- 멀티롤 사용자 1: OPERATOR + DESIGNER
(9, 9, 'OPERATOR', NOW()),
(10, 9, 'DESIGNER', NOW()),
-- 멀티롤 사용자 2: DESIGNER + INSTRUCTOR
(11, 10, 'DESIGNER', NOW()),
(12, 10, 'INSTRUCTOR', NOW());

-- ===== 테넌트 1 일반 사용자 역할 (50명) =====
INSERT INTO user_roles (id, user_id, role, created_at) VALUES
(101, 101, 'USER', NOW()),
(102, 102, 'USER', NOW()),
(103, 103, 'USER', NOW()),
(104, 104, 'USER', NOW()),
(105, 105, 'USER', NOW()),
(106, 106, 'USER', NOW()),
(107, 107, 'USER', NOW()),
(108, 108, 'USER', NOW()),
(109, 109, 'USER', NOW()),
(110, 110, 'USER', NOW()),
(111, 111, 'USER', NOW()),
(112, 112, 'USER', NOW()),
(113, 113, 'USER', NOW()),
(114, 114, 'USER', NOW()),
(115, 115, 'USER', NOW()),
(116, 116, 'USER', NOW()),
(117, 117, 'USER', NOW()),
(118, 118, 'USER', NOW()),
(119, 119, 'USER', NOW()),
(120, 120, 'USER', NOW()),
(121, 121, 'USER', NOW()),
(122, 122, 'USER', NOW()),
(123, 123, 'USER', NOW()),
(124, 124, 'USER', NOW()),
(125, 125, 'USER', NOW()),
(126, 126, 'USER', NOW()),
(127, 127, 'USER', NOW()),
(128, 128, 'USER', NOW()),
(129, 129, 'USER', NOW()),
(130, 130, 'USER', NOW()),
(131, 131, 'USER', NOW()),
(132, 132, 'USER', NOW()),
(133, 133, 'USER', NOW()),
(134, 134, 'USER', NOW()),
(135, 135, 'USER', NOW()),
(136, 136, 'USER', NOW()),
(137, 137, 'USER', NOW()),
(138, 138, 'USER', NOW()),
(139, 139, 'USER', NOW()),
(140, 140, 'USER', NOW()),
(141, 141, 'USER', NOW()),
(142, 142, 'USER', NOW()),
(143, 143, 'USER', NOW()),
(144, 144, 'USER', NOW()),
(145, 145, 'USER', NOW()),
(146, 146, 'USER', NOW()),
(147, 147, 'USER', NOW()),
(148, 148, 'USER', NOW()),
(149, 149, 'USER', NOW()),
(150, 150, 'USER', NOW()),
-- INSTRUCTOR + USER 복합롤 (팀장/차장급)
(151, 101, 'INSTRUCTOR', NOW()),  -- 김민준 (개발팀 차장)
(152, 116, 'INSTRUCTOR', NOW()),  -- 안서준 (마케팅팀 팀장)
(153, 126, 'INSTRUCTOR', NOW()),  -- 양현준 (인사팀 팀장)
(154, 134, 'INSTRUCTOR', NOW()),  -- 추성훈 (영업팀 팀장)
(155, 144, 'INSTRUCTOR', NOW());  -- 두시현 (디자인팀 팀장)

-- ===== 테넌트 2 역할 =====
INSERT INTO user_roles (id, user_id, role, created_at) VALUES
-- 관리자급
(1001, 11, 'TENANT_ADMIN', NOW()),
(1002, 12, 'OPERATOR', NOW()),
(1003, 13, 'DESIGNER', NOW()),
(1004, 14, 'USER', NOW()),
-- 일반 사용자
(1005, 1001, 'USER', NOW()),
(1006, 1002, 'USER', NOW()),
(1007, 1003, 'USER', NOW()),
(1008, 1004, 'USER', NOW()),
(1009, 1005, 'USER', NOW()),
(1010, 1006, 'USER', NOW()),
(1011, 1007, 'USER', NOW()),
(1012, 1008, 'USER', NOW()),
(1013, 1009, 'USER', NOW()),
(1014, 1010, 'USER', NOW()),
(1015, 1011, 'USER', NOW()),
(1016, 1012, 'USER', NOW()),
(1017, 1013, 'USER', NOW()),
(1018, 1014, 'USER', NOW()),
(1019, 1015, 'USER', NOW()),
(1020, 1016, 'USER', NOW()),
(1021, 1017, 'USER', NOW()),
(1022, 1018, 'USER', NOW()),
(1023, 1019, 'USER', NOW()),
(1024, 1020, 'USER', NOW()),
(1025, 1021, 'USER', NOW()),
(1026, 1022, 'USER', NOW()),
(1027, 1023, 'USER', NOW()),
(1028, 1024, 'USER', NOW()),
(1029, 1025, 'USER', NOW()),
(1030, 1026, 'USER', NOW()),
(1031, 1027, 'USER', NOW()),
(1032, 1028, 'USER', NOW()),
(1033, 1029, 'USER', NOW()),
(1034, 1030, 'USER', NOW()),
-- INSTRUCTOR + USER 복합롤
(1035, 1024, 'INSTRUCTOR', NOW()),  -- 하지원 (개발팀 차장)
(1036, 1025, 'INSTRUCTOR', NOW()),  -- 문재현 (기획팀 팀장)
(1037, 1028, 'INSTRUCTOR', NOW());  -- 남지현 (개발팀 팀장)

-- ===== 테넌트 3 역할 =====
INSERT INTO user_roles (id, user_id, role, created_at) VALUES
-- 관리자급
(2001, 21, 'TENANT_ADMIN', NOW()),
(2002, 22, 'OPERATOR', NOW()),
(2003, 23, 'DESIGNER', NOW()),
(2004, 24, 'USER', NOW()),
-- 일반 사용자
(2005, 2001, 'USER', NOW()),
(2006, 2002, 'USER', NOW()),
(2007, 2003, 'USER', NOW()),
(2008, 2004, 'USER', NOW()),
(2009, 2005, 'USER', NOW()),
(2010, 2006, 'USER', NOW()),
(2011, 2007, 'USER', NOW()),
(2012, 2008, 'USER', NOW()),
(2013, 2009, 'USER', NOW()),
(2014, 2010, 'USER', NOW()),
(2015, 2011, 'USER', NOW()),
(2016, 2012, 'USER', NOW()),
(2017, 2013, 'USER', NOW()),
(2018, 2014, 'USER', NOW()),
(2019, 2015, 'USER', NOW()),
(2020, 2016, 'USER', NOW()),
(2021, 2017, 'USER', NOW()),
(2022, 2018, 'USER', NOW()),
(2023, 2019, 'USER', NOW()),
(2024, 2020, 'USER', NOW()),
(2025, 2021, 'USER', NOW()),
(2026, 2022, 'USER', NOW()),
(2027, 2023, 'USER', NOW()),
(2028, 2024, 'USER', NOW()),
(2029, 2025, 'USER', NOW()),
(2030, 2026, 'USER', NOW()),
(2031, 2027, 'USER', NOW()),
(2032, 2028, 'USER', NOW()),
(2033, 2029, 'USER', NOW()),
(2034, 2030, 'USER', NOW()),
-- INSTRUCTOR + USER 복합롤
(2035, 2019, 'INSTRUCTOR', NOW()),  -- 홍현우 (개발팀 팀장)
(2036, 2022, 'INSTRUCTOR', NOW()),  -- 백도윤 (개발팀 차장)
(2037, 2024, 'INSTRUCTOR', NOW());  -- 하준서 (운영팀 팀장)
