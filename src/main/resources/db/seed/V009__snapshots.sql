-- =============================================
-- V009: 스냅샷 데이터 (명시적 ID)
-- =============================================
-- 스냅샷 + LO + 아이템 + 관계 데이터
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 스냅샷 (각 차수별 고유 스냅샷) =====
-- 차수(course_time)와 스냅샷은 1:1 관계이므로 차수 수만큼 스냅샷 필요
INSERT INTO cm_snapshots (id, tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
-- 차수 1: Spring Boot 기초 1차 (DRAFT)
(1, 1, 10, 'Spring Boot 기초 v1.0', 'Spring Boot 프레임워크의 기본 개념과 실습', 'ACTIVE', 1, 3, NOW() - INTERVAL 25 DAY, NOW()),
-- 차수 2: AWS 클라우드 아키텍처 1차 (DRAFT)
(2, 1, 11, 'AWS 클라우드 아키텍처 v1.0', 'AWS 서비스를 활용한 클라우드 인프라 설계', 'ACTIVE', 1, 3, NOW() - INTERVAL 23 DAY, NOW()),
-- 차수 3: Spring Boot 기초 2차 (RECRUITING)
(3, 1, 10, 'Spring Boot 기초 v1.1', 'Spring Boot 프레임워크의 기본 개념과 실습 - 개선판', 'ACTIVE', 1, 3, NOW() - INTERVAL 20 DAY, NOW()),
-- 차수 4: React & TypeScript 실전 1차 (RECRUITING)
(4, 1, 12, 'React & TypeScript 실전 v1.0', 'React와 TypeScript를 활용한 프론트엔드 개발', 'ACTIVE', 1, 3, NOW() - INTERVAL 20 DAY, NOW()),
-- 차수 5: AWS 클라우드 아키텍처 2차 (RECRUITING)
(5, 1, 11, 'AWS 클라우드 아키텍처 v1.1', 'AWS 서비스를 활용한 클라우드 인프라 설계 - 개선판', 'ACTIVE', 1, 4, NOW() - INTERVAL 18 DAY, NOW()),
-- 차수 6: Spring Boot 기초 3차 (ONGOING)
(6, 1, 10, 'Spring Boot 기초 v1.2', 'Spring Boot 프레임워크의 기본 개념과 실습 - 최신판', 'ACTIVE', 1, 3, NOW() - INTERVAL 40 DAY, NOW()),
-- 차수 7: Java 프로그래밍 마스터 1차 (ONGOING)
(7, 1, 13, 'Java 프로그래밍 마스터 v1.0', '자바 언어의 핵심 개념부터 고급 기능까지', 'ACTIVE', 1, 3, NOW() - INTERVAL 35 DAY, NOW()),
-- 차수 8: Kubernetes 운영 실무 1차 (ONGOING)
(8, 1, 14, 'Kubernetes 운영 실무 v1.0', '쿠버네티스 클러스터 운영 및 모니터링', 'ACTIVE', 1, 4, NOW() - INTERVAL 50 DAY, NOW()),
-- 차수 9: Spring Boot 기초 4차 (CLOSED)
(9, 1, 10, 'Spring Boot 기초 v0.9', 'Spring Boot 프레임워크의 기본 개념과 실습 - 이전 버전', 'ARCHIVED', 1, 3, NOW() - INTERVAL 100 DAY, NOW()),
-- 차수 10: SQL 완전 정복 1차 (CLOSED)
(10, 1, 15, 'SQL 완전 정복 v1.0', '관계형 데이터베이스와 SQL 쿼리 작성', 'ARCHIVED', 1, 4, NOW() - INTERVAL 60 DAY, NOW()),
-- 차수 11: Spring Boot 기초 0차 파일럿 (ARCHIVED)
(11, 1, 10, 'Spring Boot 기초 v0.1 파일럿', 'Spring Boot 프레임워크의 기본 개념 - 파일럿 과정', 'ARCHIVED', 1, 3, NOW() - INTERVAL 365 DAY, NOW()),
-- 차수 12: Java 프로그래밍 마스터 0차 (ARCHIVED)
(12, 1, 13, 'Java 프로그래밍 마스터 v0.9', '자바 언어의 핵심 개념 - 초기 버전', 'ARCHIVED', 1, 4, NOW() - INTERVAL 300 DAY, NOW()),
-- 차수 13: Kubernetes 운영 실무 특별반 (INVITE_ONLY + RECRUITING)
(13, 1, 14, 'Kubernetes 운영 실무 특별반 v1.0', '쿠버네티스 클러스터 운영 심화 - 선발제 과정', 'ACTIVE', 1, 4, NOW() - INTERVAL 10 DAY, NOW()),
-- 차수 14: React & TypeScript 심화 특별반 (APPROVAL + RECRUITING)
(14, 1, 12, 'React & TypeScript 심화 특별반 v1.0', 'React와 TypeScript를 활용한 프론트엔드 심화 - 승인제 과정', 'ACTIVE', 1, 3, NOW() - INTERVAL 10 DAY, NOW()),
-- 차수 15: SQL 완전 정복 2차 (FIRST_COME + RECRUITING)
(15, 1, 15, 'SQL 완전 정복 v2.0', '관계형 데이터베이스와 SQL 쿼리 작성 - 개선판', 'ACTIVE', 1, 4, NOW() - INTERVAL 5 DAY, NOW());

-- ===== 테넌트 1 스냅샷 LO (원본 LO ID 참조 포함) =====
INSERT INTO cm_snapshot_los (id, tenant_id, source_lo_id, content_id, display_name, duration, thumbnail_url, is_customized, created_at, updated_at, version) VALUES
-- Spring Boot 스냅샷 LO (source_lo_id = LearningObject.id)
(1, 1, 1, 1, 'Spring Boot 소개', 1800, '/thumbnails/spring-boot-intro.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(2, 1, 2, 2, '프로젝트 생성하기', 2400, '/thumbnails/spring-boot-setup.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(3, 1, 3, 3, 'REST API 구현', 3600, '/thumbnails/spring-boot-rest.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(4, 1, 4, 4, 'JPA와 데이터베이스', 4200, '/thumbnails/spring-boot-jpa.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
-- AWS 스냅샷 LO
(5, 1, 5, 5, 'AWS 클라우드 개요', 2700, '/thumbnails/aws-overview.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(6, 1, 6, 6, 'EC2 인스턴스 생성', 3000, '/thumbnails/aws-ec2.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(7, 1, 7, 7, 'S3 스토리지 활용', 2400, '/thumbnails/aws-s3.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
-- React 스냅샷 LO
(8, 1, 8, 8, 'React 컴포넌트 기초', 3000, '/thumbnails/react-components.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(9, 1, 9, 9, 'TypeScript 타입 시스템', 3600, '/thumbnails/typescript-types.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(10, 1, 10, 10, 'React Hooks 심화', 4500, '/thumbnails/react-hooks.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
-- Java 스냅샷 LO
(11, 1, 11, 11, 'Java 기본 문법', 3600, '/thumbnails/java-basics.jpg', false, NOW() - INTERVAL 30 DAY, NOW(), 0),
(12, 1, 12, 12, '객체지향 프로그래밍', 5400, '/thumbnails/java-oop.jpg', false, NOW() - INTERVAL 30 DAY, NOW(), 0),
-- Kubernetes 스냅샷 LO
(13, 1, 13, 13, 'Kubernetes 아키텍처', 4200, '/thumbnails/k8s-architecture.jpg', false, NOW() - INTERVAL 35 DAY, NOW(), 0),
(14, 1, 14, 14, 'Pod와 Deployment', 3600, '/thumbnails/k8s-pods.jpg', false, NOW() - INTERVAL 35 DAY, NOW(), 0),
-- SQL 스냅샷 LO
(15, 1, 15, 15, 'SELECT 문 기초', 1800, '/thumbnails/sql-select.jpg', false, NOW() - INTERVAL 15 DAY, NOW(), 0),
(16, 1, 16, 16, 'JOIN과 서브쿼리', 2700, '/thumbnails/sql-join.jpg', false, NOW() - INTERVAL 15 DAY, NOW(), 0),
-- 테스트용 스냅샷 LO (스냅샷 13, 14, 15용)
(17, 1, 13, 13, 'Kubernetes 아키텍처 심화', 5400, '/thumbnails/k8s-advanced.jpg', false, NOW() - INTERVAL 10 DAY, NOW(), 0),
(18, 1, 14, 14, 'Pod와 Deployment 심화', 4800, '/thumbnails/k8s-pods-adv.jpg', false, NOW() - INTERVAL 10 DAY, NOW(), 0),
(19, 1, 8, 8, 'React 심화 컴포넌트', 4200, '/thumbnails/react-adv.jpg', false, NOW() - INTERVAL 10 DAY, NOW(), 0),
(20, 1, 15, 15, 'SQL 심화 쿼리', 3000, '/thumbnails/sql-adv.jpg', false, NOW() - INTERVAL 5 DAY, NOW(), 0);

-- ===== 테넌트 1 스냅샷 아이템 (커리큘럼 구조) =====
INSERT INTO cm_snapshot_items (id, tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
-- Spring Boot 스냅샷 - 루트 폴더
(1, 1, 1, NULL, '1장. Spring Boot 시작하기', 0, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0),
(2, 1, 1, NULL, '2장. REST API 개발', 0, NULL, NULL, NOW() - INTERVAL 25 DAY, NOW(), 0),
-- Spring Boot 스냅샷 - 1장 하위 아이템
(3, 1, 1, 1, 'Spring Boot 소개', 1, 'VIDEO', 1, NOW() - INTERVAL 25 DAY, NOW(), 0),
(4, 1, 1, 1, '프로젝트 생성하기', 1, 'VIDEO', 2, NOW() - INTERVAL 25 DAY, NOW(), 0),
-- Spring Boot 스냅샷 - 2장 하위 아이템
(5, 1, 1, 2, 'REST API 구현', 1, 'VIDEO', 3, NOW() - INTERVAL 25 DAY, NOW(), 0),
(6, 1, 1, 2, 'JPA와 데이터베이스', 1, 'VIDEO', 4, NOW() - INTERVAL 25 DAY, NOW(), 0),
-- AWS 스냅샷 아이템
(7, 1, 4, NULL, 'AWS 기초', 0, NULL, NULL, NOW() - INTERVAL 23 DAY, NOW(), 0),
(8, 1, 4, 7, 'AWS 클라우드 개요', 1, 'VIDEO', 5, NOW() - INTERVAL 23 DAY, NOW(), 0),
(9, 1, 4, 7, 'EC2 인스턴스 생성', 1, 'VIDEO', 6, NOW() - INTERVAL 23 DAY, NOW(), 0),
(10, 1, 4, 7, 'S3 스토리지 활용', 1, 'VIDEO', 7, NOW() - INTERVAL 23 DAY, NOW(), 0),
-- React 스냅샷 아이템 (폴더 없이 직접 배치)
(11, 1, 2, NULL, 'React 컴포넌트 기초', 0, 'VIDEO', 8, NOW() - INTERVAL 20 DAY, NOW(), 0),
(12, 1, 2, NULL, 'TypeScript 타입 시스템', 0, 'VIDEO', 9, NOW() - INTERVAL 20 DAY, NOW(), 0),
(13, 1, 2, NULL, 'React Hooks 심화', 0, 'VIDEO', 10, NOW() - INTERVAL 20 DAY, NOW(), 0),
-- Java 스냅샷 아이템
(14, 1, 3, NULL, 'Java 기본 문법', 0, 'VIDEO', 11, NOW() - INTERVAL 30 DAY, NOW(), 0),
(15, 1, 3, NULL, '객체지향 프로그래밍', 0, 'VIDEO', 12, NOW() - INTERVAL 30 DAY, NOW(), 0),
-- Kubernetes 스냅샷 아이템
(16, 1, 5, NULL, 'Kubernetes 아키텍처', 0, 'VIDEO', 13, NOW() - INTERVAL 35 DAY, NOW(), 0),
(17, 1, 5, NULL, 'Pod와 Deployment', 0, 'VIDEO', 14, NOW() - INTERVAL 35 DAY, NOW(), 0),
-- SQL 스냅샷 아이템
(18, 1, 6, NULL, 'SELECT 문 기초', 0, 'VIDEO', 15, NOW() - INTERVAL 15 DAY, NOW(), 0),
(19, 1, 6, NULL, 'JOIN과 서브쿼리', 0, 'VIDEO', 16, NOW() - INTERVAL 15 DAY, NOW(), 0),
-- 테스트용 스냅샷 아이템 (스냅샷 13, 14, 15)
-- 스냅샷 13: Kubernetes 특별반
(20, 1, 13, NULL, 'Kubernetes 아키텍처 심화', 0, 'VIDEO', 17, NOW() - INTERVAL 10 DAY, NOW(), 0),
(21, 1, 13, NULL, 'Pod와 Deployment 심화', 0, 'VIDEO', 18, NOW() - INTERVAL 10 DAY, NOW(), 0),
-- 스냅샷 14: React & TypeScript 심화 특별반
(22, 1, 14, NULL, 'React 심화 컴포넌트', 0, 'VIDEO', 19, NOW() - INTERVAL 10 DAY, NOW(), 0),
-- 스냅샷 15: SQL 완전 정복 2차
(23, 1, 15, NULL, 'SQL 심화 쿼리', 0, 'VIDEO', 20, NOW() - INTERVAL 5 DAY, NOW(), 0);

-- ===== 테넌트 1 스냅샷 관계 (학습 순서) =====
INSERT INTO cm_snapshot_relations (id, tenant_id, snapshot_id, from_item_id, to_item_id, created_at, updated_at, version) VALUES
-- Spring Boot 스냅샷 학습 순서
(1, 1, 1, NULL, 3, NOW(), NOW(), 0),
(2, 1, 1, 3, 4, NOW(), NOW(), 0),
(3, 1, 1, 4, 5, NOW(), NOW(), 0),
(4, 1, 1, 5, 6, NOW(), NOW(), 0),
-- React 스냅샷 학습 순서
(5, 1, 2, NULL, 11, NOW(), NOW(), 0),
(6, 1, 2, 11, 12, NOW(), NOW(), 0),
(7, 1, 2, 12, 13, NOW(), NOW(), 0),
-- Java 스냅샷 학습 순서
(8, 1, 3, NULL, 14, NOW(), NOW(), 0),
(9, 1, 3, 14, 15, NOW(), NOW(), 0);

-- ===== 테넌트 2 스냅샷 (차수별 고유 스냅샷) =====
INSERT INTO cm_snapshots (id, tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
-- 차수 51: Java 기초 입문 1차 (RECRUITING)
(51, 2, 51, 'Java 기초 입문 v1.0', 'Java 프로그래밍 언어의 기본 개념 학습', 'ACTIVE', 1, 12, NOW() - INTERVAL 25 DAY, NOW()),
-- 차수 52: Spring Framework 실전 1차 (RECRUITING)
(52, 2, 52, 'Spring Framework 실전 v1.0', 'Spring Framework를 활용한 엔터프라이즈 애플리케이션 개발', 'ACTIVE', 1, 12, NOW() - INTERVAL 23 DAY, NOW()),
-- 차수 53: 클라우드 네이티브 개발 1차 (ONGOING)
(53, 2, 53, '클라우드 네이티브 개발 v1.0', '클라우드 환경에 최적화된 애플리케이션 개발 방법론', 'ACTIVE', 1, 12, NOW() - INTERVAL 30 DAY, NOW()),
-- 차수 54: 머신러닝 입문 1차 (ONGOING)
(54, 2, 54, '머신러닝 입문 v1.0', '머신러닝의 기초 개념과 실습', 'ACTIVE', 1, 12, NOW() - INTERVAL 28 DAY, NOW()),
-- 차수 55: 딥러닝 심화 1차 (DRAFT)
(55, 2, 55, '딥러닝 심화 v1.0', '신경망과 딥러닝 알고리즘 학습', 'ACTIVE', 1, 12, NOW() - INTERVAL 15 DAY, NOW()),
-- 차수 56: Java 기초 입문 2차 (CLOSED)
(56, 2, 51, 'Java 기초 입문 v0.9', 'Java 프로그래밍 언어의 기본 개념 학습 - 이전 버전', 'ARCHIVED', 1, 12, NOW() - INTERVAL 90 DAY, NOW()),
-- 차수 57: 클라우드 네이티브 특별반 (INVITE_ONLY + RECRUITING)
(57, 2, 53, '클라우드 네이티브 특별반 v1.0', '클라우드 환경 최적화 개발 심화 - 선발제 과정', 'ACTIVE', 1, 12, NOW() - INTERVAL 10 DAY, NOW()),
-- 차수 58: 머신러닝 심화 특별반 (APPROVAL + RECRUITING)
(58, 2, 54, '머신러닝 심화 특별반 v1.0', '머신러닝 심화 과정 - 승인제 과정', 'ACTIVE', 1, 12, NOW() - INTERVAL 10 DAY, NOW()),
-- 차수 59: Spring Framework 2차 (FIRST_COME + RECRUITING)
(59, 2, 52, 'Spring Framework v2.0', 'Spring Framework 실전 개발 - 개선판', 'ACTIVE', 1, 12, NOW() - INTERVAL 5 DAY, NOW());

-- ===== 테넌트 2 스냅샷 LO (원본 LO ID 참조 포함) =====
INSERT INTO cm_snapshot_los (id, tenant_id, source_lo_id, content_id, display_name, duration, thumbnail_url, is_customized, created_at, updated_at, version) VALUES
(51, 2, 101, 101, 'Java 기초 입문', 2700, '/thumbnails/t2-java-intro.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(52, 2, 102, 102, 'Spring Framework', 3600, '/thumbnails/t2-spring.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(53, 2, 103, 103, '클라우드 네이티브', 3000, '/thumbnails/t2-cloud-native.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(54, 2, 104, 104, '머신러닝 기초', 4200, '/thumbnails/t2-ml-basics.jpg', false, NOW() - INTERVAL 18 DAY, NOW(), 0),
-- 테스트용 스냅샷 LO (스냅샷 57, 58, 59용)
(55, 2, 103, 103, '클라우드 네이티브 심화', 4500, '/thumbnails/t2-cloud-adv.jpg', false, NOW() - INTERVAL 10 DAY, NOW(), 0),
(56, 2, 104, 104, '머신러닝 심화', 5400, '/thumbnails/t2-ml-adv.jpg', false, NOW() - INTERVAL 10 DAY, NOW(), 0),
(57, 2, 102, 102, 'Spring Framework 심화', 4200, '/thumbnails/t2-spring-adv.jpg', false, NOW() - INTERVAL 5 DAY, NOW(), 0);

-- ===== 테넌트 2 스냅샷 아이템 =====
INSERT INTO cm_snapshot_items (id, tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(51, 2, 51, NULL, 'Java 기초 입문', 0, 'VIDEO', 51, NOW() - INTERVAL 25 DAY, NOW(), 0),
(52, 2, 52, NULL, 'Spring Framework', 0, 'VIDEO', 52, NOW() - INTERVAL 23 DAY, NOW(), 0),
(53, 2, 53, NULL, '클라우드 네이티브', 0, 'VIDEO', 53, NOW() - INTERVAL 20 DAY, NOW(), 0),
(54, 2, 54, NULL, '머신러닝 기초', 0, 'VIDEO', 54, NOW() - INTERVAL 18 DAY, NOW(), 0),
-- 테스트용 스냅샷 아이템 (스냅샷 57, 58, 59)
(55, 2, 57, NULL, '클라우드 네이티브 심화', 0, 'VIDEO', 55, NOW() - INTERVAL 10 DAY, NOW(), 0),
(56, 2, 58, NULL, '머신러닝 심화', 0, 'VIDEO', 56, NOW() - INTERVAL 10 DAY, NOW(), 0),
(57, 2, 59, NULL, 'Spring Framework 심화', 0, 'VIDEO', 57, NOW() - INTERVAL 5 DAY, NOW(), 0);

-- ===== 테넌트 3 스냅샷 (차수별 고유 스냅샷) =====
INSERT INTO cm_snapshots (id, tenant_id, source_course_id, snapshot_name, description, status, version, created_by, created_at, updated_at) VALUES
-- 차수 101: React Native 시작하기 1차 (RECRUITING)
(101, 3, 101, 'React Native 시작하기 v1.0', '크로스 플랫폼 모바일 앱 개발 입문', 'ACTIVE', 1, 22, NOW() - INTERVAL 25 DAY, NOW()),
-- 차수 102: Flutter 개발 1차 (RECRUITING)
(102, 3, 102, 'Flutter 개발 v1.0', 'Flutter를 활용한 모바일 앱 개발', 'ACTIVE', 1, 22, NOW() - INTERVAL 23 DAY, NOW()),
-- 차수 103: 웹 개발 풀스택 1차 (ONGOING)
(103, 3, 103, '웹 개발 풀스택 v1.0', '프론트엔드부터 백엔드까지 웹 개발 전체 과정', 'ACTIVE', 1, 22, NOW() - INTERVAL 30 DAY, NOW()),
-- 차수 104: 데이터 시각화 1차 (ONGOING)
(104, 3, 104, '데이터 시각화 v1.0', '효과적인 데이터 시각화 기법', 'ACTIVE', 1, 22, NOW() - INTERVAL 28 DAY, NOW()),
-- 차수 105: React Native 시작하기 0차 (CLOSED)
(105, 3, 101, 'React Native 시작하기 v0.9', '크로스 플랫폼 모바일 앱 개발 - 이전 버전', 'ARCHIVED', 1, 22, NOW() - INTERVAL 120 DAY, NOW()),
-- 차수 106: 웹 개발 풀스택 0차 (ARCHIVED)
(106, 3, 103, '웹 개발 풀스택 v0.9', '웹 개발 전체 과정 - 이전 버전', 'ARCHIVED', 1, 22, NOW() - INTERVAL 180 DAY, NOW());

-- ===== 테넌트 3 스냅샷 LO (원본 LO ID 참조 포함) =====
INSERT INTO cm_snapshot_los (id, tenant_id, source_lo_id, content_id, display_name, duration, thumbnail_url, is_customized, created_at, updated_at, version) VALUES
(101, 3, 201, 201, 'React Native 시작하기', 2400, '/thumbnails/t3-rn-intro.jpg', false, NOW() - INTERVAL 25 DAY, NOW(), 0),
(102, 3, 202, 202, 'Flutter 개발', 3000, '/thumbnails/t3-flutter.jpg', false, NOW() - INTERVAL 23 DAY, NOW(), 0),
(103, 3, 203, 203, '웹 개발 풀스택', 4500, '/thumbnails/t3-fullstack.jpg', false, NOW() - INTERVAL 20 DAY, NOW(), 0),
(104, 3, 204, 204, '데이터 시각화', 2700, '/thumbnails/t3-data-viz.jpg', false, NOW() - INTERVAL 18 DAY, NOW(), 0);

-- ===== 테넌트 3 스냅샷 아이템 =====
INSERT INTO cm_snapshot_items (id, tenant_id, snapshot_id, parent_id, item_name, depth, item_type, snapshot_lo_id, created_at, updated_at, version) VALUES
(101, 3, 101, NULL, 'React Native 시작하기', 0, 'VIDEO', 101, NOW() - INTERVAL 25 DAY, NOW(), 0),
(102, 3, 102, NULL, 'Flutter 개발', 0, 'VIDEO', 102, NOW() - INTERVAL 23 DAY, NOW(), 0),
(103, 3, 103, NULL, '웹 개발 풀스택', 0, 'VIDEO', 103, NOW() - INTERVAL 20 DAY, NOW(), 0),
(104, 3, 104, NULL, '데이터 시각화', 0, 'VIDEO', 104, NOW() - INTERVAL 18 DAY, NOW(), 0);
