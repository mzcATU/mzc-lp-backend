-- 기본 테넌트 초기 데이터
INSERT INTO tenants (id, code, name, type, status, plan, subdomain, created_at, updated_at)
SELECT 1, 'default', '기본 테넌트', 'B2B', 'ACTIVE', 'ENTERPRISE', 'default', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 1);

-- 시스템 어드민 계정 (syadmin@admin.com / ehfkdl3dy!)
-- BCrypt hash for 'ehfkdl3dy!': $2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'syadmin@admin.com', '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', 'SystemAdmin', '010-0000-0001', 'SYSTEM_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'syadmin@admin.com' AND tenant_id = 1);

-- 테넌트 어드민 계정 (taadmin@admin.com / ehfkdl3dy!)
INSERT INTO users (tenant_id, email, password, name, phone, role, status, created_at, updated_at)
SELECT 1, 'taadmin@admin.com', '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', 'TenantAdmin', '010-0000-0002', 'TENANT_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'taadmin@admin.com' AND tenant_id = 1);

-- 기존 관리자 계정 비밀번호 업데이트 (이미 존재하는 경우)
UPDATE users SET password = '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', role = 'SYSTEM_ADMIN' WHERE email = 'syadmin@admin.com' AND tenant_id = 1;
UPDATE users SET password = '$2a$10$KLLGIMUnCdCtvatQ231HUe8.f5wrYzGfUh0FTuMJolVBco3NE/khq', role = 'TENANT_ADMIN' WHERE email = 'taadmin@admin.com' AND tenant_id = 1;

-- 테스트 계정 역할 업데이트
UPDATE users SET role = 'DESIGNER' WHERE email = 'designer@test.com';
UPDATE users SET role = 'OPERATOR' WHERE email = 'operator@test.com';

-- @Version 필드 NULL 수정 (Hibernate 낙관적 락 지원)
UPDATE course_times SET version = 0 WHERE version IS NULL;
UPDATE sis_enrollments SET version = 0 WHERE version IS NULL;
UPDATE cm_programs SET version = 0 WHERE version IS NULL;
UPDATE cm_courses SET version = 0 WHERE version IS NULL;
UPDATE content SET version = 0 WHERE version IS NULL;
UPDATE content_folder SET version = 0 WHERE version IS NULL;
UPDATE learning_object SET version = 0 WHERE version IS NULL;
UPDATE iis_instructor_assignments SET version = 0 WHERE version IS NULL;
UPDATE cm_snapshots SET jpa_version = 0 WHERE jpa_version IS NULL;
UPDATE cm_snapshot_items SET version = 0 WHERE version IS NULL;
UPDATE cm_snapshot_relations SET version = 0 WHERE version IS NULL;
UPDATE cm_snapshot_los SET version = 0 WHERE version IS NULL;

-- ============================================
-- TU 강의 탐색용 더미 Program 데이터
-- ============================================

-- Program 1: Next.js 완벽 마스터
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 1, 1, 0, '실전! Next.js 15 완벽 마스터',
       'Next.js 15의 새로운 기능과 함께 실전 프로젝트를 만들어봅니다. App Router, Server Actions, 그리고 최신 React 패턴을 학습합니다.',
       'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=250&fit=crop',
       'INTERMEDIATE', 'ONLINE', 32, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 1);

-- Program 2: ChatGPT API 활용
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 2, 1, 0, 'ChatGPT API 활용 실무 프로젝트',
       'OpenAI API를 활용한 실무 프로젝트를 진행합니다. 챗봇, 문서 분석, 코드 생성 등 다양한 활용법을 배웁니다.',
       'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=400&h=250&fit=crop',
       'INTERMEDIATE', 'ONLINE', 28, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 2);

-- Program 3: 데이터 분석 with Python
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 3, 1, 0, '데이터 분석 with Python',
       'Pandas, NumPy, Matplotlib를 활용한 데이터 분석 기초부터 실무까지. 실제 데이터셋으로 분석 프로젝트를 진행합니다.',
       'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400&h=250&fit=crop',
       'BEGINNER', 'ONLINE', 24, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 3);

-- Program 4: Figma 마스터 클래스
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 4, 1, 0, 'Figma 마스터 클래스',
       'UI/UX 디자인 도구 Figma의 기초부터 고급 기능까지. 컴포넌트, 프로토타이핑, 디자인 시스템을 배웁니다.',
       'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400&h=250&fit=crop',
       'BEGINNER', 'ONLINE', 18, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 4);

-- Program 5: AWS 클라우드 실무
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 5, 1, 0, 'AWS 클라우드 실무',
       'EC2, S3, Lambda, RDS 등 AWS 핵심 서비스를 실습합니다. 실제 서비스 배포까지 경험해봅니다.',
       'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&h=250&fit=crop',
       'ADVANCED', 'ONLINE', 30, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 5);

-- Program 6: Spring Boot 3.0 실전
INSERT INTO cm_programs (id, tenant_id, version, title, description, thumbnail_url, level, type, estimated_hours, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 6, 1, 0, 'Spring Boot 3.0 실전',
       'Spring Boot 3.0의 새로운 기능과 함께 RESTful API를 구축합니다. JPA, Security, 테스트 코드까지.',
       'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=250&fit=crop',
       'INTERMEDIATE', 'ONLINE', 35, 'APPROVED', 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cm_programs WHERE id = 6);
