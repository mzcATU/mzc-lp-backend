-- =============================================
-- V017: 로드맵 데이터
-- =============================================
-- 학습 로드맵 및 로드맵 내 코스 순서
-- ID 범위: 테넌트 1: 1-20, 테넌트 2: 21-40, 테넌트 3: 41-60

-- ===== 테넌트 1 로드맵 =====
INSERT INTO roadmaps (id, tenant_id, title, description, category, thumbnail_url, is_active, created_by, created_at, updated_at) VALUES
(1, 1, '백엔드 개발자 로드맵', 'Java와 Spring Boot를 활용한 백엔드 개발자가 되기 위한 학습 경로입니다.', '개발', '/thumbnails/roadmap-backend.jpg', true, 3, NOW() - INTERVAL 30 DAY, NOW()),
(2, 1, '프론트엔드 개발자 로드맵', 'React와 TypeScript를 활용한 프론트엔드 개발자가 되기 위한 학습 경로입니다.', '개발', '/thumbnails/roadmap-frontend.jpg', true, 3, NOW() - INTERVAL 28 DAY, NOW()),
(3, 1, '데이터 엔지니어 로드맵', 'SQL과 Python을 활용한 데이터 엔지니어가 되기 위한 학습 경로입니다.', '데이터', '/thumbnails/roadmap-data.jpg', true, 3, NOW() - INTERVAL 25 DAY, NOW()),
(4, 1, 'DevOps 엔지니어 로드맵', '클라우드와 Kubernetes를 활용한 DevOps 엔지니어가 되기 위한 학습 경로입니다.', '인프라', '/thumbnails/roadmap-devops.jpg', true, 4, NOW() - INTERVAL 22 DAY, NOW());

-- 로드맵 내 코스 순서 (roadmap_programs)
-- 참고: cm_programs 테이블이 제거되어 course_times를 직접 참조하도록 수정
-- 기존 스키마에 따라 roadmap_programs가 어떤 FK를 참조하는지 확인 필요
-- 아래는 예시 데이터입니다

-- ===== 테넌트 2 로드맵 =====
INSERT INTO roadmaps (id, tenant_id, title, description, category, thumbnail_url, is_active, created_by, created_at, updated_at) VALUES
(21, 2, '삼성전자 신입 개발자 온보딩', '신입 개발자를 위한 필수 교육 과정입니다.', '온보딩', '/thumbnails/roadmap-samsung-onboarding.jpg', true, 12, NOW() - INTERVAL 20 DAY, NOW()),
(22, 2, '삼성전자 클라우드 전환 과정', '클라우드 기술 습득을 위한 학습 경로입니다.', '클라우드', '/thumbnails/roadmap-samsung-cloud.jpg', true, 12, NOW() - INTERVAL 18 DAY, NOW());

-- ===== 테넌트 3 로드맵 =====
INSERT INTO roadmaps (id, tenant_id, title, description, category, thumbnail_url, is_active, created_by, created_at, updated_at) VALUES
(41, 3, '네이버 모바일 개발자 과정', '모바일 앱 개발을 위한 학습 경로입니다.', '모바일', '/thumbnails/roadmap-naver-mobile.jpg', true, 22, NOW() - INTERVAL 15 DAY, NOW()),
(42, 3, '네이버 풀스택 개발자 과정', '풀스택 개발자가 되기 위한 학습 경로입니다.', '개발', '/thumbnails/roadmap-naver-fullstack.jpg', true, 22, NOW() - INTERVAL 12 DAY, NOW());
