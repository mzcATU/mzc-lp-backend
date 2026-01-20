-- =============================================
-- V007: 콘텐츠 데이터 (명시적 ID)
-- =============================================
-- 스냅샷 LO가 콘텐츠를 참조하므로 먼저 생성
-- ID 범위: 테넌트 1: 1-100, 테넌트 2: 101-200, 테넌트 3: 201-300

-- ===== 테넌트 1 콘텐츠 =====
INSERT INTO content (id, tenant_id, status, created_by, current_version, original_file_name, content_type, duration, file_path, thumbnail_path, created_at, updated_at, version) VALUES
-- Spring Boot 강의 영상
(1, 1, 'ACTIVE', 8, 1, 'Spring Boot 소개', 'VIDEO', 1800, '/videos/spring-boot-intro.mp4', '/thumbnails/spring-boot-intro.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(2, 1, 'ACTIVE', 8, 1, '프로젝트 생성하기', 'VIDEO', 2400, '/videos/spring-boot-setup.mp4', '/thumbnails/spring-boot-setup.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(3, 1, 'ACTIVE', 8, 1, 'REST API 구현', 'VIDEO', 3600, '/videos/spring-boot-rest.mp4', '/thumbnails/spring-boot-rest.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(4, 1, 'ACTIVE', 8, 1, 'JPA와 데이터베이스', 'VIDEO', 4200, '/videos/spring-boot-jpa.mp4', '/thumbnails/spring-boot-jpa.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
-- AWS 강의 영상
(5, 1, 'ACTIVE', 5, 1, 'AWS 클라우드 개요', 'VIDEO', 2700, '/videos/aws-overview.mp4', '/thumbnails/aws-overview.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(6, 1, 'ACTIVE', 5, 1, 'EC2 인스턴스 생성', 'VIDEO', 3000, '/videos/aws-ec2.mp4', '/thumbnails/aws-ec2.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(7, 1, 'ACTIVE', 5, 1, 'S3 스토리지 활용', 'VIDEO', 2400, '/videos/aws-s3.mp4', '/thumbnails/aws-s3.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
-- React 강의 영상
(8, 1, 'ACTIVE', 8, 1, 'React 컴포넌트 기초', 'VIDEO', 3000, '/videos/react-components.mp4', '/thumbnails/react-components.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(9, 1, 'ACTIVE', 8, 1, 'TypeScript 타입 시스템', 'VIDEO', 3600, '/videos/typescript-types.mp4', '/thumbnails/typescript-types.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(10, 1, 'ACTIVE', 8, 1, 'React Hooks 심화', 'VIDEO', 4500, '/videos/react-hooks.mp4', '/thumbnails/react-hooks.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
-- Java 강의 영상
(11, 1, 'ACTIVE', 5, 1, 'Java 기본 문법', 'VIDEO', 3600, '/videos/java-basics.mp4', '/thumbnails/java-basics.jpg', NOW() - INTERVAL 35 DAY, NOW(), 0),
(12, 1, 'ACTIVE', 5, 1, '객체지향 프로그래밍', 'VIDEO', 5400, '/videos/java-oop.mp4', '/thumbnails/java-oop.jpg', NOW() - INTERVAL 35 DAY, NOW(), 0),
-- Kubernetes 강의 영상
(13, 1, 'ACTIVE', 6, 1, 'Kubernetes 아키텍처', 'VIDEO', 4200, '/videos/k8s-architecture.mp4', '/thumbnails/k8s-architecture.jpg', NOW() - INTERVAL 40 DAY, NOW(), 0),
(14, 1, 'ACTIVE', 6, 1, 'Pod와 Deployment', 'VIDEO', 3600, '/videos/k8s-pods.mp4', '/thumbnails/k8s-pods.jpg', NOW() - INTERVAL 40 DAY, NOW(), 0),
-- SQL 강의 영상
(15, 1, 'ACTIVE', 6, 1, 'SELECT 문 기초', 'VIDEO', 1800, '/videos/sql-select.mp4', '/thumbnails/sql-select.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0),
(16, 1, 'ACTIVE', 6, 1, 'JOIN과 서브쿼리', 'VIDEO', 2700, '/videos/sql-join.mp4', '/thumbnails/sql-join.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0),
-- Python 강의 영상
(17, 1, 'ACTIVE', 6, 1, 'Python 기초', 'VIDEO', 2400, '/videos/python-basics.mp4', '/thumbnails/python-basics.jpg', NOW() - INTERVAL 18 DAY, NOW(), 0),
(18, 1, 'ACTIVE', 6, 1, 'Pandas 라이브러리', 'VIDEO', 3000, '/videos/python-pandas.mp4', '/thumbnails/python-pandas.jpg', NOW() - INTERVAL 18 DAY, NOW(), 0),
-- ChatGPT 강의 영상
(19, 1, 'ACTIVE', 5, 1, 'ChatGPT 기초 활용', 'VIDEO', 1500, '/videos/chatgpt-basics.mp4', '/thumbnails/chatgpt-basics.jpg', NOW() - INTERVAL 15 DAY, NOW(), 0),
(20, 1, 'ACTIVE', 5, 1, 'ChatGPT 고급 프롬프트', 'VIDEO', 2100, '/videos/chatgpt-advanced.mp4', '/thumbnails/chatgpt-advanced.jpg', NOW() - INTERVAL 15 DAY, NOW(), 0),
-- DevOps 강의 영상
(21, 1, 'ACTIVE', 5, 1, 'CI/CD 파이프라인', 'VIDEO', 3300, '/videos/devops-cicd.mp4', '/thumbnails/devops-cicd.jpg', NOW() - INTERVAL 12 DAY, NOW(), 0),
(22, 1, 'ACTIVE', 5, 1, 'Docker 컨테이너', 'VIDEO', 2700, '/videos/devops-docker.mp4', '/thumbnails/devops-docker.jpg', NOW() - INTERVAL 12 DAY, NOW(), 0);

-- ===== 테넌트 2 콘텐츠 =====
INSERT INTO content (id, tenant_id, status, created_by, current_version, original_file_name, content_type, duration, file_path, thumbnail_path, created_at, updated_at, version) VALUES
(101, 2, 'ACTIVE', 13, 1, 'Java 기초 입문', 'VIDEO', 2700, '/videos/t2-java-intro.mp4', '/thumbnails/t2-java-intro.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(102, 2, 'ACTIVE', 13, 1, 'Spring Framework', 'VIDEO', 3600, '/videos/t2-spring.mp4', '/thumbnails/t2-spring.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(103, 2, 'ACTIVE', 13, 1, '클라우드 네이티브', 'VIDEO', 3000, '/videos/t2-cloud-native.mp4', '/thumbnails/t2-cloud-native.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(104, 2, 'ACTIVE', 13, 1, '머신러닝 기초', 'VIDEO', 4200, '/videos/t2-ml-basics.mp4', '/thumbnails/t2-ml-basics.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0);

-- ===== 테넌트 3 콘텐츠 =====
INSERT INTO content (id, tenant_id, status, created_by, current_version, original_file_name, content_type, duration, file_path, thumbnail_path, created_at, updated_at, version) VALUES
(201, 3, 'ACTIVE', 23, 1, 'React Native 시작하기', 'VIDEO', 2400, '/videos/t3-rn-intro.mp4', '/thumbnails/t3-rn-intro.jpg', NOW() - INTERVAL 30 DAY, NOW(), 0),
(202, 3, 'ACTIVE', 23, 1, 'Flutter 개발', 'VIDEO', 3000, '/videos/t3-flutter.mp4', '/thumbnails/t3-flutter.jpg', NOW() - INTERVAL 28 DAY, NOW(), 0),
(203, 3, 'ACTIVE', 23, 1, '웹 개발 풀스택', 'VIDEO', 4500, '/videos/t3-fullstack.mp4', '/thumbnails/t3-fullstack.jpg', NOW() - INTERVAL 25 DAY, NOW(), 0),
(204, 3, 'ACTIVE', 23, 1, '데이터 시각화', 'VIDEO', 2700, '/videos/t3-data-viz.mp4', '/thumbnails/t3-data-viz.jpg', NOW() - INTERVAL 20 DAY, NOW(), 0);
