-- =============================================
-- V001: 테이블 초기화 (AUTO_INCREMENT 리셋)
-- =============================================
-- 모든 시드 데이터 테이블을 TRUNCATE하여 ID를 리셋합니다.
-- TRUNCATE는 DELETE와 달리 AUTO_INCREMENT를 초기화합니다.

SET FOREIGN_KEY_CHECKS = 0;

-- 커뮤니티 관련
TRUNCATE TABLE community_comment_likes;
TRUNCATE TABLE community_post_likes;
TRUNCATE TABLE community_comments;
TRUNCATE TABLE community_posts;

-- 리뷰
TRUNCATE TABLE cm_course_reviews;

-- 로드맵
TRUNCATE TABLE roadmap_programs;
TRUNCATE TABLE roadmaps;

-- 수강/강사/진도
TRUNCATE TABLE sis_item_progress;
TRUNCATE TABLE sis_enrollments;
TRUNCATE TABLE iis_instructor_assignments;

-- 사용자 역할
TRUNCATE TABLE user_course_roles;
TRUNCATE TABLE user_roles;

-- 장바구니/찜
TRUNCATE TABLE cart_items;
TRUNCATE TABLE cm_wishlist_items;

-- 차수
TRUNCATE TABLE course_times;

-- 스냅샷 관련
TRUNCATE TABLE cm_snapshot_relations;
TRUNCATE TABLE cm_snapshot_items;
TRUNCATE TABLE cm_snapshot_los;
TRUNCATE TABLE cm_snapshots;

-- 콘텐츠/러닝오브젝트
TRUNCATE TABLE learning_object;
TRUNCATE TABLE content;

-- 코스 관련
TRUNCATE TABLE cm_course_tags;
TRUNCATE TABLE cm_course_items;
TRUNCATE TABLE cm_courses;

-- 카테고리
TRUNCATE TABLE cm_categories;

-- 사용자/부서
TRUNCATE TABLE users;
TRUNCATE TABLE departments;

-- tenants는 TRUNCATE하지 않음 (WHERE NOT EXISTS로 처리)

SET FOREIGN_KEY_CHECKS = 1;
