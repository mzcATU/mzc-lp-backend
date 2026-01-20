-- =============================================
-- V007_5: 러닝오브젝트 데이터 (명시적 ID)
-- =============================================
-- Content를 감싸는 LearningObject 데이터
-- CourseItem과 SnapshotLO가 이 테이블을 참조
-- ID 범위: 테넌트 1: 1-100, 테넌트 2: 101-200, 테넌트 3: 201-300

-- ===== 테넌트 1 러닝오브젝트 =====
INSERT INTO learning_object (id, tenant_id, name, content_id, folder_id, completion_criteria, created_at, updated_at, version) VALUES
-- Spring Boot 강의 LO (Content 1-4)
(1, 1, 'Spring Boot 소개', 1, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
(2, 1, '프로젝트 생성하기', 2, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
(3, 1, 'REST API 구현', 3, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
(4, 1, 'JPA와 데이터베이스', 4, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
-- AWS 강의 LO (Content 5-7)
(5, 1, 'AWS 클라우드 개요', 5, NULL, 'PERCENT_100', NOW() - INTERVAL 28 DAY, NOW(), 0),
(6, 1, 'EC2 인스턴스 생성', 6, NULL, 'PERCENT_100', NOW() - INTERVAL 28 DAY, NOW(), 0),
(7, 1, 'S3 스토리지 활용', 7, NULL, 'PERCENT_100', NOW() - INTERVAL 28 DAY, NOW(), 0),
-- React 강의 LO (Content 8-10)
(8, 1, 'React 컴포넌트 기초', 8, NULL, 'PERCENT_100', NOW() - INTERVAL 25 DAY, NOW(), 0),
(9, 1, 'TypeScript 타입 시스템', 9, NULL, 'PERCENT_100', NOW() - INTERVAL 25 DAY, NOW(), 0),
(10, 1, 'React Hooks 심화', 10, NULL, 'PERCENT_100', NOW() - INTERVAL 25 DAY, NOW(), 0),
-- Java 강의 LO (Content 11-12)
(11, 1, 'Java 기본 문법', 11, NULL, 'PERCENT_100', NOW() - INTERVAL 35 DAY, NOW(), 0),
(12, 1, '객체지향 프로그래밍', 12, NULL, 'PERCENT_100', NOW() - INTERVAL 35 DAY, NOW(), 0),
-- Kubernetes 강의 LO (Content 13-14)
(13, 1, 'Kubernetes 아키텍처', 13, NULL, 'PERCENT_100', NOW() - INTERVAL 40 DAY, NOW(), 0),
(14, 1, 'Pod와 Deployment', 14, NULL, 'PERCENT_100', NOW() - INTERVAL 40 DAY, NOW(), 0),
-- SQL 강의 LO (Content 15-16)
(15, 1, 'SELECT 문 기초', 15, NULL, 'PERCENT_100', NOW() - INTERVAL 20 DAY, NOW(), 0),
(16, 1, 'JOIN과 서브쿼리', 16, NULL, 'PERCENT_100', NOW() - INTERVAL 20 DAY, NOW(), 0),
-- Python 강의 LO (Content 17-18)
(17, 1, 'Python 기초', 17, NULL, 'PERCENT_100', NOW() - INTERVAL 18 DAY, NOW(), 0),
(18, 1, 'Pandas 라이브러리', 18, NULL, 'PERCENT_100', NOW() - INTERVAL 18 DAY, NOW(), 0),
-- ChatGPT 강의 LO (Content 19-20)
(19, 1, 'ChatGPT 기초 활용', 19, NULL, 'PERCENT_100', NOW() - INTERVAL 15 DAY, NOW(), 0),
(20, 1, 'ChatGPT 고급 프롬프트', 20, NULL, 'PERCENT_100', NOW() - INTERVAL 15 DAY, NOW(), 0),
-- DevOps 강의 LO (Content 21-22)
(21, 1, 'CI/CD 파이프라인', 21, NULL, 'PERCENT_100', NOW() - INTERVAL 12 DAY, NOW(), 0),
(22, 1, 'Docker 컨테이너', 22, NULL, 'PERCENT_100', NOW() - INTERVAL 12 DAY, NOW(), 0);

-- ===== 테넌트 2 러닝오브젝트 =====
INSERT INTO learning_object (id, tenant_id, name, content_id, folder_id, completion_criteria, created_at, updated_at, version) VALUES
(101, 2, 'Java 기초 입문', 101, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
(102, 2, 'Spring Framework', 102, NULL, 'PERCENT_100', NOW() - INTERVAL 28 DAY, NOW(), 0),
(103, 2, '클라우드 네이티브', 103, NULL, 'PERCENT_100', NOW() - INTERVAL 25 DAY, NOW(), 0),
(104, 2, '머신러닝 기초', 104, NULL, 'PERCENT_100', NOW() - INTERVAL 20 DAY, NOW(), 0);

-- ===== 테넌트 3 러닝오브젝트 =====
INSERT INTO learning_object (id, tenant_id, name, content_id, folder_id, completion_criteria, created_at, updated_at, version) VALUES
(201, 3, 'React Native 시작하기', 201, NULL, 'PERCENT_100', NOW() - INTERVAL 30 DAY, NOW(), 0),
(202, 3, 'Flutter 개발', 202, NULL, 'PERCENT_100', NOW() - INTERVAL 28 DAY, NOW(), 0),
(203, 3, '웹 개발 풀스택', 203, NULL, 'PERCENT_100', NOW() - INTERVAL 25 DAY, NOW(), 0),
(204, 3, '데이터 시각화', 204, NULL, 'PERCENT_100', NOW() - INTERVAL 20 DAY, NOW(), 0);
