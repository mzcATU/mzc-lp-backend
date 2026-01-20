-- =============================================
-- V008: 코스 데이터 (명시적 ID, 변수 활용)
-- =============================================
-- 코스 + 태그 + 아이템 데이터
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 코스 =====
INSERT INTO cm_courses (id, tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version, status) VALUES
(1, 1, 'Spring Boot 기초', 'Spring Boot 프레임워크의 기본 개념과 실습', 'BEGINNER', 'ONLINE', 20, 1, 8, NOW() - INTERVAL 30 DAY, NOW(), 0, 'REGISTERED'),
(2, 1, 'React & TypeScript', 'React와 TypeScript를 활용한 프론트엔드 개발', 'ADVANCED', 'ONLINE', 30, 1, 8, NOW() - INTERVAL 25 DAY, NOW(), 0, 'REGISTERED'),
(3, 1, 'Java 프로그래밍', '자바 언어의 핵심 개념부터 고급 기능까지', 'INTERMEDIATE', 'ONLINE', 45, 1, 5, NOW() - INTERVAL 35 DAY, NOW(), 0, 'REGISTERED'),
(4, 1, 'Kubernetes 운영', '쿠버네티스 클러스터 운영 및 모니터링', 'ADVANCED', 'BLENDED', 50, 1, 6, NOW() - INTERVAL 40 DAY, NOW(), 0, 'REGISTERED'),
(5, 1, 'DevOps 엔지니어링', 'CI/CD 파이프라인 구축과 컨테이너 오케스트레이션', 'INTERMEDIATE', 'OFFLINE', 35, 1, 5, NOW() - INTERVAL 18 DAY, NOW(), 0, 'REGISTERED'),
(6, 1, 'ChatGPT 활용법', 'ChatGPT를 업무에 활용하는 방법', 'BEGINNER', 'ONLINE', 10, 2, 5, NOW() - INTERVAL 28 DAY, NOW(), 0, 'REGISTERED'),
(7, 1, 'SQL 완전 정복', '관계형 데이터베이스와 SQL 쿼리 작성', 'BEGINNER', 'ONLINE', 15, 3, 6, NOW() - INTERVAL 20 DAY, NOW(), 0, 'REGISTERED'),
(8, 1, 'Python 데이터 분석', 'Pandas, NumPy를 활용한 데이터 분석', 'BEGINNER', 'ONLINE', 25, 3, 6, NOW() - INTERVAL 15 DAY, NOW(), 0, 'REGISTERED'),
(9, 1, 'AWS 클라우드 아키텍처', 'AWS 서비스를 활용한 클라우드 인프라 설계', 'INTERMEDIATE', 'BLENDED', 40, 5, 5, NOW() - INTERVAL 28 DAY, NOW(), 0, 'REGISTERED');

-- 코스명으로 조회하기 위한 프로그램 호환 코스 추가 (기존 data.sql과의 호환성)
INSERT INTO cm_courses (id, tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version, status) VALUES
(10, 1, 'Spring Boot 기초 과정', 'Spring Boot 프레임워크의 기본 개념과 실습을 다루는 입문 과정입니다.', 'BEGINNER', 'ONLINE', 20, 1, 8, NOW() - INTERVAL 14 DAY, NOW(), 0, 'REGISTERED'),
(11, 1, 'AWS 클라우드 아키텍처', 'AWS 서비스를 활용한 클라우드 인프라 설계 및 구축 과정입니다.', 'INTERMEDIATE', 'BLENDED', 40, 5, 5, NOW() - INTERVAL 12 DAY, NOW(), 0, 'REGISTERED'),
(12, 1, 'React & TypeScript 실전', 'React와 TypeScript를 활용한 프론트엔드 개발 심화 과정입니다.', 'ADVANCED', 'ONLINE', 30, 1, 8, NOW() - INTERVAL 10 DAY, NOW(), 0, 'REGISTERED'),
(13, 1, 'Java 프로그래밍 마스터', '자바 언어의 핵심 개념부터 고급 기능까지 체계적으로 학습합니다.', 'INTERMEDIATE', 'ONLINE', 45, 1, 5, NOW() - INTERVAL 16 DAY, NOW(), 0, 'REGISTERED'),
(14, 1, 'Kubernetes 운영 실무', '쿠버네티스 클러스터 운영 및 모니터링 실무 과정입니다.', 'ADVANCED', 'BLENDED', 50, 1, 6, NOW() - INTERVAL 20 DAY, NOW(), 0, 'REGISTERED'),
(15, 1, 'SQL 완전 정복', '관계형 데이터베이스와 SQL 쿼리 작성의 모든 것을 다룹니다.', 'BEGINNER', 'ONLINE', 15, 3, 6, NOW() - INTERVAL 10 DAY, NOW(), 0, 'REGISTERED');

-- ===== 테넌트 1 코스 태그 =====
INSERT INTO cm_course_tags (course_id, tag) VALUES
-- Spring Boot 기초
(1, 'Spring'), (1, 'Java'), (1, 'Backend'),
-- React & TypeScript
(2, 'React'), (2, 'TypeScript'), (2, 'Frontend'),
-- Java 프로그래밍
(3, 'Java'), (3, 'OOP'), (3, 'Backend'),
-- Kubernetes 운영
(4, 'Kubernetes'), (4, 'DevOps'), (4, 'Cloud'),
-- DevOps 엔지니어링
(5, 'DevOps'), (5, 'CI/CD'), (5, 'Docker'),
-- ChatGPT 활용법
(6, 'AI'), (6, 'ChatGPT'), (6, 'Productivity'),
-- SQL 완전 정복
(7, 'SQL'), (7, 'Database'), (7, 'Data'),
-- Python 데이터 분석
(8, 'Python'), (8, 'Data'), (8, 'Pandas'),
-- AWS 클라우드 아키텍처
(9, 'AWS'), (9, 'Cloud'), (9, 'Infrastructure'),
-- 프로그램 호환 코스 태그
(10, 'Spring'), (10, 'Java'), (10, 'Backend'),
(11, 'AWS'), (11, 'Cloud'),
(12, 'React'), (12, 'TypeScript'), (12, 'Frontend'),
(13, 'Java'), (13, 'OOP'),
(14, 'Kubernetes'), (14, 'DevOps'),
(15, 'SQL'), (15, 'Database');

-- ===== 테넌트 1 코스 아이템 (커리큘럼 구조) =====
-- 폴더(learning_object_id=NULL)와 학습 아이템(learning_object_id 지정)으로 구성
INSERT INTO cm_course_items (id, tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at) VALUES
-- ===== Spring Boot 기초 (코스 1) =====
-- 폴더
(1, 1, 1, NULL, NULL, '1장. Spring Boot 시작하기', NULL, NULL, 0, NOW(), NOW()),
(2, 1, 1, NULL, NULL, '2장. REST API 개발', NULL, NULL, 0, NOW(), NOW()),
-- 1장 하위 학습 아이템
(3, 1, 1, 1, 1, 'Spring Boot 소개', 'Spring Boot 프레임워크 소개', 'Spring Boot의 핵심 개념과 장점을 학습합니다', 1, NOW(), NOW()),
(4, 1, 1, 1, 2, '프로젝트 생성하기', '프로젝트 초기 설정', 'Spring Initializr를 활용한 프로젝트 생성 방법', 1, NOW(), NOW()),
-- 2장 하위 학습 아이템
(5, 1, 1, 2, 3, 'REST API 구현', 'REST API 설계와 구현', 'RESTful 웹 서비스 개발 방법론', 1, NOW(), NOW()),
(6, 1, 1, 2, 4, 'JPA와 데이터베이스', 'JPA 기초와 활용', '데이터베이스 연동과 ORM 활용', 1, NOW(), NOW()),

-- ===== React & TypeScript (코스 2) =====
(10, 1, 2, NULL, NULL, '1장. React 기초', NULL, NULL, 0, NOW(), NOW()),
(11, 1, 2, NULL, NULL, '2장. TypeScript 심화', NULL, NULL, 0, NOW(), NOW()),
-- 1장 하위 학습 아이템
(12, 1, 2, 10, 8, 'React 컴포넌트 기초', '컴포넌트 설계 원칙', 'React 컴포넌트의 기본 개념과 설계 패턴', 1, NOW(), NOW()),
-- 2장 하위 학습 아이템
(13, 1, 2, 11, 9, 'TypeScript 타입 시스템', '타입 안전한 개발', 'TypeScript의 타입 시스템 깊이 이해하기', 1, NOW(), NOW()),
(14, 1, 2, 11, 10, 'React Hooks 심화', 'Hooks 패턴 마스터', 'useState, useEffect 등 Hooks 활용 심화', 1, NOW(), NOW()),

-- ===== Java 프로그래밍 (코스 3) =====
(20, 1, 3, NULL, NULL, '1장. Java 기본 문법', NULL, NULL, 0, NOW(), NOW()),
(21, 1, 3, NULL, NULL, '2장. 객체지향 프로그래밍', NULL, NULL, 0, NOW(), NOW()),
-- 하위 학습 아이템
(22, 1, 3, 20, 11, 'Java 기본 문법', 'Java 언어 기초', 'Java 프로그래밍 언어의 기본 문법 학습', 1, NOW(), NOW()),
(23, 1, 3, 21, 12, '객체지향 프로그래밍', 'OOP 개념 마스터', '클래스, 상속, 다형성 등 OOP 핵심 개념', 1, NOW(), NOW()),

-- ===== Kubernetes 운영 (코스 4) =====
(30, 1, 4, NULL, NULL, '1장. Kubernetes 기초', NULL, NULL, 0, NOW(), NOW()),
(31, 1, 4, 30, 13, 'Kubernetes 아키텍처', 'K8s 아키텍처 이해', '쿠버네티스 클러스터 아키텍처와 구성요소', 1, NOW(), NOW()),
(32, 1, 4, 30, 14, 'Pod와 Deployment', 'Pod 관리', 'Pod와 Deployment 리소스 관리 방법', 1, NOW(), NOW()),

-- ===== DevOps 엔지니어링 (코스 5) =====
(40, 1, 5, NULL, NULL, '1장. CI/CD와 컨테이너', NULL, NULL, 0, NOW(), NOW()),
(41, 1, 5, 40, 21, 'CI/CD 파이프라인', 'CI/CD 구축', '지속적 통합/배포 파이프라인 설계', 1, NOW(), NOW()),
(42, 1, 5, 40, 22, 'Docker 컨테이너', 'Docker 기초', 'Docker 컨테이너 기술 활용', 1, NOW(), NOW()),

-- ===== ChatGPT 활용법 (코스 6) =====
(50, 1, 6, NULL, 19, 'ChatGPT 기초 활용', 'AI 비서 활용하기', 'ChatGPT를 업무에 활용하는 기초 방법', 0, NOW(), NOW()),
(51, 1, 6, NULL, 20, 'ChatGPT 고급 프롬프트', '프롬프트 엔지니어링', '효과적인 프롬프트 작성 기법', 0, NOW(), NOW()),

-- ===== SQL 완전 정복 (코스 7) =====
(60, 1, 7, NULL, NULL, '1장. SQL 기초', NULL, NULL, 0, NOW(), NOW()),
(61, 1, 7, 60, 15, 'SELECT 문 기초', '데이터 조회', 'SQL SELECT 문의 기초와 활용', 1, NOW(), NOW()),
(62, 1, 7, 60, 16, 'JOIN과 서브쿼리', '복잡한 쿼리 작성', '테이블 조인과 서브쿼리 활용', 1, NOW(), NOW()),

-- ===== Python 데이터 분석 (코스 8) =====
(70, 1, 8, NULL, NULL, '1장. Python 데이터 분석', NULL, NULL, 0, NOW(), NOW()),
(71, 1, 8, 70, 17, 'Python 기초', 'Python 언어 기초', 'Python 프로그래밍 기초 학습', 1, NOW(), NOW()),
(72, 1, 8, 70, 18, 'Pandas 라이브러리', '데이터 처리', 'Pandas를 활용한 데이터 분석', 1, NOW(), NOW()),

-- ===== AWS 클라우드 아키텍처 (코스 9) =====
(80, 1, 9, NULL, NULL, '1장. AWS 기초', NULL, NULL, 0, NOW(), NOW()),
(81, 1, 9, 80, 5, 'AWS 클라우드 개요', 'AWS 서비스 소개', 'AWS 클라우드 서비스 전체 개요', 1, NOW(), NOW()),
(82, 1, 9, 80, 6, 'EC2 인스턴스 생성', '가상 서버 운영', 'EC2 인스턴스 생성과 관리', 1, NOW(), NOW()),
(83, 1, 9, 80, 7, 'S3 스토리지 활용', '오브젝트 스토리지', 'S3 버킷 생성과 활용 방법', 1, NOW(), NOW());

-- ===== 테넌트 2 코스 (20개) =====
INSERT INTO cm_courses (id, tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version, status) VALUES
(51, 2, 'Java 기초 입문', 'Java 프로그래밍 언어의 기본 개념 학습', 'BEGINNER', 'ONLINE', 20, 11, 13, NOW() - INTERVAL 30 DAY, NOW(), 0, 'REGISTERED'),
(52, 2, 'Spring Framework 실전', 'Spring Framework를 활용한 엔터프라이즈 애플리케이션 개발', 'INTERMEDIATE', 'ONLINE', 35, 11, 13, NOW() - INTERVAL 28 DAY, NOW(), 0, 'REGISTERED'),
(53, 2, '클라우드 네이티브 개발', '클라우드 환경에 최적화된 애플리케이션 개발 방법론', 'ADVANCED', 'BLENDED', 40, 14, 13, NOW() - INTERVAL 25 DAY, NOW(), 0, 'REGISTERED'),
(54, 2, '머신러닝 입문', '머신러닝의 기초 개념과 실습', 'BEGINNER', 'ONLINE', 30, 12, 13, NOW() - INTERVAL 22 DAY, NOW(), 0, 'REGISTERED'),
(55, 2, '딥러닝 심화', '신경망과 딥러닝 알고리즘 학습', 'ADVANCED', 'ONLINE', 45, 12, 13, NOW() - INTERVAL 20 DAY, NOW(), 0, 'REGISTERED'),
(56, 2, 'Python 기초', 'Python 프로그래밍 언어 입문', 'BEGINNER', 'ONLINE', 15, 11, 13, NOW() - INTERVAL 18 DAY, NOW(), 0, 'REGISTERED'),
(57, 2, '데이터 엔지니어링', '데이터 파이프라인 구축 및 운영', 'INTERMEDIATE', 'BLENDED', 35, 13, 13, NOW() - INTERVAL 16 DAY, NOW(), 0, 'REGISTERED'),
(58, 2, 'AWS 입문', 'AWS 클라우드 서비스 기초', 'BEGINNER', 'ONLINE', 20, 14, 13, NOW() - INTERVAL 14 DAY, NOW(), 0, 'REGISTERED'),
(59, 2, '정보보안 기초', '사이버 보안의 기본 개념과 실습', 'BEGINNER', 'ONLINE', 25, 15, 13, NOW() - INTERVAL 12 DAY, NOW(), 0, 'REGISTERED'),
(60, 2, '네트워크 보안', '네트워크 보안 아키텍처와 방어 기법', 'INTERMEDIATE', 'BLENDED', 30, 15, 13, NOW() - INTERVAL 10 DAY, NOW(), 0, 'REGISTERED'),
(61, 2, 'Docker & Kubernetes', '컨테이너 기술과 오케스트레이션', 'INTERMEDIATE', 'ONLINE', 35, 14, 13, NOW() - INTERVAL 8 DAY, NOW(), 0, 'REGISTERED'),
(62, 2, 'React 개발', 'React를 활용한 프론트엔드 개발', 'INTERMEDIATE', 'ONLINE', 30, 11, 13, NOW() - INTERVAL 6 DAY, NOW(), 0, 'REGISTERED'),
(63, 2, 'Node.js 백엔드', 'Node.js를 활용한 백엔드 개발', 'INTERMEDIATE', 'ONLINE', 28, 11, 13, NOW() - INTERVAL 5 DAY, NOW(), 0, 'REGISTERED'),
(64, 2, 'SQL 고급', '고급 SQL 쿼리 작성과 성능 최적화', 'ADVANCED', 'ONLINE', 20, 13, 13, NOW() - INTERVAL 4 DAY, NOW(), 0, 'REGISTERED'),
(65, 2, 'Git & GitHub', '버전 관리와 협업 도구', 'BEGINNER', 'ONLINE', 10, 11, 13, NOW() - INTERVAL 3 DAY, NOW(), 0, 'REGISTERED'),
(66, 2, '애자일 방법론', '애자일 개발 방법론과 스크럼', 'BEGINNER', 'OFFLINE', 15, 11, 13, NOW() - INTERVAL 2 DAY, NOW(), 0, 'REGISTERED'),
(67, 2, 'TDD 실전', '테스트 주도 개발 실습', 'ADVANCED', 'ONLINE', 25, 11, 13, NOW() - INTERVAL 1 DAY, NOW(), 0, 'REGISTERED'),
(68, 2, 'MSA 설계', '마이크로서비스 아키텍처 설계', 'ADVANCED', 'BLENDED', 40, 11, 13, NOW(), NOW(), 0, 'REGISTERED'),
(69, 2, '데이터 분석 기초', 'Python을 활용한 데이터 분석 입문', 'BEGINNER', 'ONLINE', 22, 13, 13, NOW(), NOW(), 0, 'REGISTERED'),
(70, 2, 'API 설계', 'RESTful API 설계 원칙과 베스트 프랙티스', 'INTERMEDIATE', 'ONLINE', 18, 11, 13, NOW(), NOW(), 0, 'REGISTERED');

-- ===== 테넌트 2 코스 태그 =====
INSERT INTO cm_course_tags (course_id, tag) VALUES
(51, 'Java'), (51, 'Programming'), (51, 'Beginner'),
(52, 'Spring'), (52, 'Java'), (52, 'Enterprise'),
(53, 'Cloud'), (53, 'DevOps'), (53, 'Architecture'),
(54, 'ML'), (54, 'AI'), (54, 'Python'),
(55, 'DeepLearning'), (55, 'AI'), (55, 'TensorFlow');

-- ===== 테넌트 3 코스 (20개) =====
INSERT INTO cm_courses (id, tenant_id, title, description, level, type, estimated_hours, category_id, created_by, created_at, updated_at, version, status) VALUES
(101, 3, 'React Native 시작하기', '크로스 플랫폼 모바일 앱 개발 입문', 'BEGINNER', 'ONLINE', 25, 23, 23, NOW() - INTERVAL 30 DAY, NOW(), 0, 'REGISTERED'),
(102, 3, 'Flutter 개발', 'Flutter를 활용한 모바일 앱 개발', 'INTERMEDIATE', 'ONLINE', 30, 23, 23, NOW() - INTERVAL 28 DAY, NOW(), 0, 'REGISTERED'),
(103, 3, '웹 개발 풀스택', '프론트엔드부터 백엔드까지 웹 개발 전체 과정', 'INTERMEDIATE', 'BLENDED', 50, 22, 23, NOW() - INTERVAL 25 DAY, NOW(), 0, 'REGISTERED'),
(104, 3, '데이터 시각화', '효과적인 데이터 시각화 기법', 'BEGINNER', 'ONLINE', 20, 24, 23, NOW() - INTERVAL 22 DAY, NOW(), 0, 'REGISTERED'),
(105, 3, 'HTML/CSS 기초', '웹 퍼블리싱의 기초', 'BEGINNER', 'ONLINE', 15, 22, 23, NOW() - INTERVAL 20 DAY, NOW(), 0, 'REGISTERED'),
(106, 3, 'JavaScript 심화', '자바스크립트 고급 기능과 패턴', 'ADVANCED', 'ONLINE', 35, 21, 23, NOW() - INTERVAL 18 DAY, NOW(), 0, 'REGISTERED'),
(107, 3, 'Vue.js 개발', 'Vue.js를 활용한 프론트엔드 개발', 'INTERMEDIATE', 'ONLINE', 28, 22, 23, NOW() - INTERVAL 16 DAY, NOW(), 0, 'REGISTERED'),
(108, 3, 'iOS 개발 입문', 'Swift를 활용한 iOS 앱 개발', 'BEGINNER', 'ONLINE', 30, 23, 23, NOW() - INTERVAL 14 DAY, NOW(), 0, 'REGISTERED'),
(109, 3, 'Android 개발 입문', 'Kotlin을 활용한 Android 앱 개발', 'BEGINNER', 'ONLINE', 30, 23, 23, NOW() - INTERVAL 12 DAY, NOW(), 0, 'REGISTERED'),
(110, 3, '통계 기초', '데이터 분석을 위한 통계학 기초', 'BEGINNER', 'ONLINE', 20, 24, 23, NOW() - INTERVAL 10 DAY, NOW(), 0, 'REGISTERED'),
(111, 3, 'Excel 데이터 분석', 'Excel을 활용한 데이터 분석', 'BEGINNER', 'ONLINE', 15, 24, 23, NOW() - INTERVAL 8 DAY, NOW(), 0, 'REGISTERED'),
(112, 3, '제품 기획', '제품 기획의 기초와 실습', 'BEGINNER', 'OFFLINE', 20, 25, 23, NOW() - INTERVAL 6 DAY, NOW(), 0, 'REGISTERED'),
(113, 3, 'UX 디자인', '사용자 경험 디자인의 원칙과 실습', 'INTERMEDIATE', 'BLENDED', 25, 25, 23, NOW() - INTERVAL 5 DAY, NOW(), 0, 'REGISTERED'),
(114, 3, '프로젝트 관리', '효과적인 프로젝트 관리 방법론', 'INTERMEDIATE', 'OFFLINE', 22, 25, 23, NOW() - INTERVAL 4 DAY, NOW(), 0, 'REGISTERED'),
(115, 3, 'SQL 기초', '데이터베이스와 SQL 쿼리 작성', 'BEGINNER', 'ONLINE', 18, 24, 23, NOW() - INTERVAL 3 DAY, NOW(), 0, 'REGISTERED'),
(116, 3, 'Python 기초', 'Python 프로그래밍 언어 입문', 'BEGINNER', 'ONLINE', 20, 21, 23, NOW() - INTERVAL 2 DAY, NOW(), 0, 'REGISTERED'),
(117, 3, '알고리즘 기초', '프로그래밍 알고리즘의 기초', 'INTERMEDIATE', 'ONLINE', 30, 21, 23, NOW() - INTERVAL 1 DAY, NOW(), 0, 'REGISTERED'),
(118, 3, '자료구조', '컴퓨터 과학의 핵심 자료구조', 'INTERMEDIATE', 'ONLINE', 28, 21, 23, NOW(), NOW(), 0, 'REGISTERED'),
(119, 3, 'API 연동', 'REST API 연동과 활용', 'INTERMEDIATE', 'ONLINE', 16, 22, 23, NOW(), NOW(), 0, 'REGISTERED'),
(120, 3, '테스팅 기초', '소프트웨어 테스팅의 기초', 'BEGINNER', 'ONLINE', 12, 21, 23, NOW(), NOW(), 0, 'REGISTERED');

-- ===== 테넌트 3 코스 태그 =====
INSERT INTO cm_course_tags (course_id, tag) VALUES
(101, 'ReactNative'), (101, 'Mobile'), (101, 'CrossPlatform'),
(102, 'Flutter'), (102, 'Dart'), (102, 'Mobile'),
(103, 'Fullstack'), (103, 'Web'), (103, 'Development'),
(104, 'DataViz'), (104, 'Analytics'), (104, 'Charts');

-- ===== 테넌트 2 코스 아이템 =====
INSERT INTO cm_course_items (id, tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at) VALUES
(501, 2, 51, NULL, 101, 'Java 기초 입문', 'Java 언어 기초', 'Java 프로그래밍 언어의 기본 개념 학습', 0, NOW(), NOW()),
(502, 2, 52, NULL, 102, 'Spring Framework', 'Spring 기초', 'Spring Framework 핵심 개념과 활용', 0, NOW(), NOW()),
(503, 2, 53, NULL, 103, '클라우드 네이티브', '클라우드 네이티브 개발', '클라우드 환경 최적화 개발 방법론', 0, NOW(), NOW()),
(504, 2, 54, NULL, 104, '머신러닝 기초', 'ML 입문', '머신러닝의 기초 개념과 실습', 0, NOW(), NOW());

-- ===== 테넌트 3 코스 아이템 =====
INSERT INTO cm_course_items (id, tenant_id, course_id, parent_id, learning_object_id, item_name, display_name, description, depth, created_at, updated_at) VALUES
(601, 3, 101, NULL, 201, 'React Native 시작하기', '크로스 플랫폼 개발', '모바일 앱 개발 입문', 0, NOW(), NOW()),
(602, 3, 102, NULL, 202, 'Flutter 개발', 'Flutter 기초', 'Flutter를 활용한 앱 개발', 0, NOW(), NOW()),
(603, 3, 103, NULL, 203, '웹 개발 풀스택', '풀스택 개발', '프론트엔드부터 백엔드까지', 0, NOW(), NOW()),
(604, 3, 104, NULL, 204, '데이터 시각화', '시각화 기법', '효과적인 데이터 시각화', 0, NOW(), NOW());
