-- =============================================
-- V014: 커뮤니티 데이터 (게시글, 댓글, 좋아요)
-- =============================================
-- ID 범위:
--   게시글: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150
--   댓글: 테넌트 1: 1-100, 테넌트 2: 101-200, 테넌트 3: 201-300
--
-- PostType: QUESTION, DISCUSSION, TIP, REVIEW, ANNOUNCEMENT

-- ===== 테넌트 1 게시글 (20개) =====
INSERT INTO community_posts (id, tenant_id, type, category, author_id, title, content, view_count, is_pinned, is_solved, is_private, tags, course_time_id, created_at, updated_at) VALUES
(1, 1, 'TIP', 'backend', 101, 'Spring Boot 3.0 새로운 기능 정리', 'Spring Boot 3.0의 주요 변경사항과 새로운 기능들을 정리해봤습니다. Java 17 기반으로 변경되면서 많은 것들이 바뀌었네요.', 245, false, false, false, 'Spring,Java,Backend', NULL, NOW() - INTERVAL 20 DAY, NOW()),
(2, 1, 'TIP', 'frontend', 102, 'React 18 Concurrent Features 활용법', 'React 18에서 도입된 Concurrent Features를 프로젝트에 적용한 경험을 공유합니다.', 189, false, false, false, 'React,Frontend', NULL, NOW() - INTERVAL 18 DAY, NOW()),
(3, 1, 'TIP', 'frontend', 103, 'TypeScript 5.0 마이그레이션 가이드', 'TypeScript 5.0으로 마이그레이션하면서 겪은 이슈들과 해결 방법입니다.', 156, false, false, false, 'TypeScript,Migration', NULL, NOW() - INTERVAL 17 DAY, NOW()),
(4, 1, 'TIP', 'cloud', 104, 'AWS Lambda 비용 최적화 팁', 'Lambda 함수의 비용을 50% 줄인 방법을 공유합니다. Provisioned Concurrency 활용법 포함.', 312, true, false, false, 'AWS,Lambda,Cost', NULL, NOW() - INTERVAL 15 DAY, NOW()),
(5, 1, 'TIP', 'devops', 105, 'Kubernetes HPA 설정 가이드', 'Pod Auto Scaling을 위한 HPA 설정 방법과 주의사항입니다.', 178, false, false, false, 'Kubernetes,DevOps', NULL, NOW() - INTERVAL 14 DAY, NOW()),
(6, 1, 'TIP', 'database', 106, 'SQL 쿼리 최적화 사례', '실무에서 경험한 SQL 쿼리 최적화 사례들을 모아봤습니다.', 267, false, false, false, 'SQL,Database,Optimization', NULL, NOW() - INTERVAL 13 DAY, NOW()),
(7, 1, 'TIP', 'data', 107, 'Python 데이터 분석 입문자를 위한 팁', 'Pandas, NumPy 처음 배우시는 분들을 위한 실용적인 팁들입니다.', 198, false, false, false, 'Python,Pandas,NumPy', NULL, NOW() - INTERVAL 12 DAY, NOW()),
(8, 1, 'DISCUSSION', 'general', 108, 'DevOps 문화 도입 경험기', '스타트업에서 DevOps 문화를 도입한 1년간의 여정을 공유합니다.', 234, false, false, false, 'DevOps,Culture', NULL, NOW() - INTERVAL 11 DAY, NOW()),
(9, 1, 'QUESTION', 'backend', 109, 'JPA N+1 문제 완벽 해결', 'JPA 사용시 자주 겪는 N+1 문제의 원인과 해결 방법입니다.', 289, false, true, false, 'JPA,Hibernate,N+1', NULL, NOW() - INTERVAL 10 DAY, NOW()),
(10, 1, 'TIP', 'devops', 110, 'Docker Compose 실전 활용', '로컬 개발 환경을 Docker Compose로 구성하는 방법입니다.', 145, false, false, false, 'Docker,DevOps', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(11, 1, 'DISCUSSION', 'general', 111, 'Git 브랜치 전략 비교', 'Git Flow, GitHub Flow, Trunk-Based Development 비교 분석입니다.', 176, false, false, false, 'Git,Workflow', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(12, 1, 'TIP', 'frontend', 112, 'Next.js 13 App Router 사용기', 'Next.js 13의 App Router로 마이그레이션한 경험을 공유합니다.', 167, false, false, false, 'Next.js,React', NULL, NOW() - INTERVAL 7 DAY, NOW()),
(13, 1, 'TIP', 'backend', 113, 'Redis Cache 설계 패턴', '효율적인 Redis Cache 설계 패턴과 주의사항입니다.', 198, false, false, false, 'Redis,Cache', NULL, NOW() - INTERVAL 6 DAY, NOW()),
(14, 1, 'DISCUSSION', 'backend', 114, 'GraphQL vs REST API', 'GraphQL과 REST API의 장단점을 실제 프로젝트 경험으로 비교합니다.', 234, false, false, false, 'GraphQL,REST,API', NULL, NOW() - INTERVAL 5 DAY, NOW()),
(15, 1, 'TIP', 'architecture', 115, 'MSA 전환 시 주의사항', '모놀리틱에서 MSA로 전환할 때 알아야 할 것들입니다.', 256, true, false, false, 'MSA,Architecture', NULL, NOW() - INTERVAL 4 DAY, NOW()),
(16, 1, 'TIP', 'devops', 116, 'Terraform IaC 입문', 'Infrastructure as Code 시작하기 - Terraform 기초 가이드입니다.', 145, false, false, false, 'Terraform,IaC', NULL, NOW() - INTERVAL 3 DAY, NOW()),
(17, 1, 'TIP', 'testing', 117, 'Jest 테스트 작성 가이드', 'Jest로 효과적인 단위 테스트 작성하는 방법입니다.', 123, false, false, false, 'Jest,Testing', NULL, NOW() - INTERVAL 2 DAY, NOW()),
(18, 1, 'TIP', 'ai', 118, 'ChatGPT API 활용 사례', 'ChatGPT API를 활용한 서비스 구축 경험을 공유합니다.', 312, true, false, false, 'ChatGPT,AI,API', NULL, NOW() - INTERVAL 1 DAY, NOW()),
(19, 1, 'TIP', 'backend', 119, 'Spring Security 6 변경사항', 'Spring Security 6의 주요 변경사항과 마이그레이션 가이드입니다.', 187, false, false, false, 'Spring,Security', NULL, NOW() - INTERVAL 12 HOUR, NOW()),
(20, 1, 'TIP', 'backend', 120, 'Kotlin Coroutine 실전 활용', 'Kotlin Coroutine을 실제 프로젝트에 적용한 경험입니다.', 156, false, false, false, 'Kotlin,Coroutine', NULL, NOW() - INTERVAL 6 HOUR, NOW());

-- ===== 테넌트 1 댓글 =====
INSERT INTO community_comments (id, tenant_id, post_id, author_id, content, parent_id, created_at, updated_at) VALUES
(1, 1, 1, 121, '정리 감사합니다! Java 17 마이그레이션 참고하겠습니다.', NULL, NOW() - INTERVAL 19 DAY, NOW()),
(2, 1, 1, 122, '저도 최근에 3.0으로 업그레이드했는데 Native Image 지원이 인상적이었어요.', NULL, NOW() - INTERVAL 18 DAY, NOW()),
(3, 1, 1, 123, '혹시 GraalVM Native Image 빌드 시간이 오래 걸리진 않나요?', 2, NOW() - INTERVAL 18 DAY, NOW()),
(4, 1, 2, 124, 'Concurrent Features 도입 후 성능 개선이 체감되나요?', NULL, NOW() - INTERVAL 17 DAY, NOW()),
(5, 1, 2, 125, '좋은 글 감사합니다. Suspense와 함께 사용하면 더 좋더라고요.', NULL, NOW() - INTERVAL 17 DAY, NOW()),
(6, 1, 4, 126, 'Lambda 비용 최적화 팁 정말 유용하네요!', NULL, NOW() - INTERVAL 14 DAY, NOW()),
(7, 1, 4, 127, 'Provisioned Concurrency 비용이 걱정되는데 어떤가요?', NULL, NOW() - INTERVAL 14 DAY, NOW()),
(8, 1, 4, 128, 'Cold Start 문제 해결에 큰 도움이 됐습니다.', NULL, NOW() - INTERVAL 13 DAY, NOW()),
(9, 1, 9, 129, 'N+1 문제 때문에 고생했는데 덕분에 해결했습니다!', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(10, 1, 9, 130, 'Fetch Join 사용시 주의할 점도 정리해주시면 좋을 것 같아요.', NULL, NOW() - INTERVAL 9 DAY, NOW()),
(11, 1, 18, 131, 'ChatGPT API 비용이 어느 정도 나오나요?', NULL, NOW() - INTERVAL 20 HOUR, NOW()),
(12, 1, 18, 132, '프롬프트 엔지니어링 팁도 공유해주세요!', NULL, NOW() - INTERVAL 18 HOUR, NOW());

-- ===== 테넌트 1 게시글 좋아요 =====
INSERT INTO community_post_likes (id, tenant_id, post_id, user_id, created_at) VALUES
(1, 1, 1, 121, NOW() - INTERVAL 19 DAY),
(2, 1, 1, 122, NOW() - INTERVAL 19 DAY),
(3, 1, 1, 123, NOW() - INTERVAL 18 DAY),
(4, 1, 4, 124, NOW() - INTERVAL 14 DAY),
(5, 1, 4, 125, NOW() - INTERVAL 14 DAY),
(6, 1, 9, 126, NOW() - INTERVAL 9 DAY),
(7, 1, 9, 127, NOW() - INTERVAL 9 DAY),
(8, 1, 18, 128, NOW() - INTERVAL 1 DAY),
(9, 1, 18, 129, NOW() - INTERVAL 1 DAY),
(10, 1, 18, 130, NOW() - INTERVAL 12 HOUR);

-- ===== 테넌트 1 댓글 좋아요 =====
INSERT INTO community_comment_likes (id, tenant_id, comment_id, user_id, created_at) VALUES
(1, 1, 1, 131, NOW() - INTERVAL 18 DAY),
(2, 1, 1, 132, NOW() - INTERVAL 18 DAY),
(3, 1, 6, 133, NOW() - INTERVAL 13 DAY),
(4, 1, 9, 134, NOW() - INTERVAL 8 DAY),
(5, 1, 9, 135, NOW() - INTERVAL 8 DAY);

-- ===== 테넌트 2 게시글 (5개) =====
INSERT INTO community_posts (id, tenant_id, type, category, author_id, title, content, view_count, is_pinned, is_solved, is_private, tags, course_time_id, created_at, updated_at) VALUES
(51, 2, 'ANNOUNCEMENT', 'general', 1001, '삼성전자 개발팀 기술 스택 소개', '우리 팀에서 사용하는 기술 스택을 소개합니다.', 156, true, false, false, 'TechStack', NULL, NOW() - INTERVAL 15 DAY, NOW()),
(52, 2, 'TIP', 'backend', 1002, 'Spring Batch 도입 후기', 'Spring Batch를 도입하면서 겪은 이슈들입니다.', 134, false, false, false, 'Spring,Batch', NULL, NOW() - INTERVAL 12 DAY, NOW()),
(53, 2, 'DISCUSSION', 'cloud', 1003, '클라우드 마이그레이션 경험', 'On-premise에서 AWS로 마이그레이션한 경험입니다.', 198, false, false, false, 'AWS,Migration', NULL, NOW() - INTERVAL 10 DAY, NOW()),
(54, 2, 'DISCUSSION', 'general', 1004, '코드 리뷰 문화 정착기', '효율적인 코드 리뷰 문화를 만들기 위한 노력들입니다.', 145, false, false, false, 'CodeReview,Culture', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(55, 2, 'TIP', 'devops', 1005, 'Jenkins vs GitHub Actions', 'CI/CD 도구 비교 및 선택 기준입니다.', 167, false, false, false, 'CI/CD,Jenkins,GitHub', NULL, NOW() - INTERVAL 5 DAY, NOW());

-- ===== 테넌트 3 게시글 (5개) =====
INSERT INTO community_posts (id, tenant_id, type, category, author_id, title, content, view_count, is_pinned, is_solved, is_private, tags, course_time_id, created_at, updated_at) VALUES
(101, 3, 'DISCUSSION', 'mobile', 2001, 'React Native vs Flutter 비교', '모바일 앱 개발 프레임워크 비교입니다.', 234, false, false, false, 'ReactNative,Flutter,Mobile', NULL, NOW() - INTERVAL 15 DAY, NOW()),
(102, 3, 'REVIEW', 'general', 2002, '스타트업 개발자 생존기', '네이버에서 1년간 개발자로 일한 경험입니다.', 289, true, false, false, 'Startup,Career', NULL, NOW() - INTERVAL 12 DAY, NOW()),
(103, 3, 'TIP', 'general', 2003, 'MVP 빠르게 만들기', '2주 만에 MVP를 만든 방법을 공유합니다.', 178, false, false, false, 'MVP,Startup', NULL, NOW() - INTERVAL 10 DAY, NOW()),
(104, 3, 'TIP', 'mobile', 2004, '앱 스토어 최적화 ASO', '앱 스토어 순위를 올리기 위한 ASO 전략입니다.', 145, false, false, false, 'ASO,Mobile,Marketing', NULL, NOW() - INTERVAL 8 DAY, NOW()),
(105, 3, 'TIP', 'backend', 2005, 'Firebase 활용 가이드', 'Firebase로 백엔드 없이 앱 만들기', 156, false, false, false, 'Firebase,Backend', NULL, NOW() - INTERVAL 5 DAY, NOW());
