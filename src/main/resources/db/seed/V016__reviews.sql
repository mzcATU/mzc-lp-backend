-- =============================================
-- V016: 리뷰/수강평 데이터
-- =============================================
-- 완료된 차수에 대한 리뷰
-- ID 범위: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 리뷰 (종료된 차수에 대한 리뷰) =====
INSERT INTO cm_course_reviews (id, tenant_id, user_id, course_time_id, rating, content, version, completion_rate, created_at, updated_at) VALUES
-- Spring Boot 기초 4차 (CLOSED) 리뷰
(1, 1, 103, 9, 5, '정말 유익한 강의였습니다. 실무에 바로 적용할 수 있는 내용이 많아서 좋았어요. 강사님의 설명도 매우 명확했습니다.', 0, 100, NOW() - INTERVAL 3 DAY, NOW()),
(2, 1, 104, 9, 4, '전반적으로 좋은 강의였습니다. 다만 고급 주제에 대한 내용이 조금 더 있었으면 좋겠어요.', 0, 100, NOW() - INTERVAL 4 DAY, NOW()),
(3, 1, 111, 9, 5, 'Spring Boot 입문자에게 최고의 강의! 기초부터 차근차근 설명해주셔서 쉽게 이해할 수 있었습니다.', 0, 100, NOW() - INTERVAL 2 DAY, NOW()),
(4, 1, 112, 9, 4, '실습 예제가 많아서 좋았습니다. 프로젝트 기반 학습이 효과적이었어요.', 0, 100, NOW() - INTERVAL 5 DAY, NOW()),

-- SQL 완전 정복 1차 (CLOSED) 리뷰
(5, 1, 117, 10, 5, 'SQL의 기초부터 고급까지 체계적으로 배울 수 있었습니다. 특히 JOIN과 서브쿼리 부분이 명확했어요.', 0, 100, NOW() - INTERVAL 8 DAY, NOW()),
(6, 1, 118, 10, 4, '실무에서 자주 사용하는 쿼리 패턴을 많이 배웠습니다. 성능 최적화 부분이 인상적이었어요.', 0, 100, NOW() - INTERVAL 7 DAY, NOW()),
(7, 1, 119, 10, 5, '데이터베이스를 처음 배우는 사람도 이해하기 쉬운 설명이었습니다. 강추합니다!', 0, 100, NOW() - INTERVAL 9 DAY, NOW()),

-- Spring Boot 기초 0차 (ARCHIVED) 리뷰
(8, 1, 143, 11, 4, '파일럿 과정이었지만 내용이 알찼습니다. 피드백을 잘 반영해주셔서 감사해요.', 0, 100, NOW() - INTERVAL 90 DAY, NOW()),
(9, 1, 144, 11, 4, '초기 버전이라 개선할 점이 있었지만, 전반적으로 만족스러운 강의였습니다.', 0, 100, NOW() - INTERVAL 88 DAY, NOW()),

-- Java 프로그래밍 마스터 0차 (ARCHIVED) 리뷰
(10, 1, 145, 12, 5, 'Java의 핵심 개념을 깊이 있게 이해할 수 있었습니다. 객체지향 설계 부분이 특히 좋았어요.', 0, 100, NOW() - INTERVAL 60 DAY, NOW());

-- ===== 테넌트 2 리뷰 =====
INSERT INTO cm_course_reviews (id, tenant_id, user_id, course_time_id, rating, content, version, completion_rate, created_at, updated_at) VALUES
-- Java 기초 입문 2차 (CLOSED) 리뷰
(51, 2, 1009, 56, 5, '프로그래밍 입문자에게 최적화된 강의입니다. 기초부터 탄탄하게 배울 수 있었어요.', 0, 100, NOW() - INTERVAL 8 DAY, NOW()),
(52, 2, 1010, 56, 4, '좋은 강의였습니다. 실습 예제가 더 많았으면 좋겠어요.', 0, 100, NOW() - INTERVAL 10 DAY, NOW()),
(53, 2, 1021, 56, 5, 'Java를 처음 배우는 직원들에게 추천합니다. 설명이 매우 친절해요.', 0, 100, NOW() - INTERVAL 9 DAY, NOW()),
(54, 2, 1022, 56, 4, '회사 교육으로 들었는데 실무에 바로 적용할 수 있는 내용이 많았습니다.', 0, 100, NOW() - INTERVAL 7 DAY, NOW());

-- ===== 테넌트 3 리뷰 =====
INSERT INTO cm_course_reviews (id, tenant_id, user_id, course_time_id, rating, content, version, completion_rate, created_at, updated_at) VALUES
-- React Native 시작하기 0차 (CLOSED) 리뷰
(101, 3, 2010, 105, 5, '모바일 앱 개발의 기초를 배우기에 최적의 강의입니다. 크로스 플랫폼의 장점을 잘 이해할 수 있었어요.', 0, 100, NOW() - INTERVAL 12 DAY, NOW()),
(102, 3, 2011, 105, 4, 'React Native의 기본 개념을 잘 설명해주셨습니다. 실제 앱 배포까지 다뤄주셔서 좋았어요.', 0, 100, NOW() - INTERVAL 10 DAY, NOW()),
(103, 3, 2022, 105, 5, '스타트업에서 빠르게 앱을 개발해야 할 때 유용한 기술입니다. 강추!', 0, 100, NOW() - INTERVAL 11 DAY, NOW()),

-- 웹 개발 풀스택 0차 (ARCHIVED) 리뷰
(104, 3, 2023, 106, 5, '프론트엔드부터 백엔드까지 전체 흐름을 이해할 수 있는 좋은 강의였습니다.', 0, 100, NOW() - INTERVAL 50 DAY, NOW()),
(105, 3, 2024, 106, 4, '풀스택 개발자를 꿈꾸는 분들에게 추천합니다. 다양한 기술을 한 번에 배울 수 있어요.', 0, 100, NOW() - INTERVAL 48 DAY, NOW());
