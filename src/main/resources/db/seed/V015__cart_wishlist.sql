-- =============================================
-- V015: 장바구니 및 찜 데이터
-- =============================================
-- ID 범위:
--   장바구니: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150
--   찜: 테넌트 1: 1-50, 테넌트 2: 51-100, 테넌트 3: 101-150

-- ===== 테넌트 1 장바구니 =====
INSERT INTO cart_items (id, tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(1, 1, 125, 3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(2, 1, 126, 4, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(3, 1, 127, 3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(4, 1, 128, 5, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(5, 1, 129, 4, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, NOW()),
(6, 1, 130, 3, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR, NOW()),
(7, 1, 131, 5, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR, NOW()),
(8, 1, 132, 4, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, NOW());

-- ===== 테넌트 1 찜 =====
INSERT INTO cm_wishlist_items (id, tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(1, 1, 133, 3, NOW() - INTERVAL 5 DAY, NOW()),
(2, 1, 134, 4, NOW() - INTERVAL 4 DAY, NOW()),
(3, 1, 135, 5, NOW() - INTERVAL 4 DAY, NOW()),
(4, 1, 136, 3, NOW() - INTERVAL 3 DAY, NOW()),
(5, 1, 137, 4, NOW() - INTERVAL 3 DAY, NOW()),
(6, 1, 138, 5, NOW() - INTERVAL 2 DAY, NOW()),
(7, 1, 139, 3, NOW() - INTERVAL 2 DAY, NOW()),
(8, 1, 140, 4, NOW() - INTERVAL 1 DAY, NOW()),
(9, 1, 141, 5, NOW() - INTERVAL 1 DAY, NOW()),
(10, 1, 142, 3, NOW() - INTERVAL 12 HOUR, NOW());

-- ===== 테넌트 2 장바구니 =====
INSERT INTO cart_items (id, tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(51, 2, 1011, 51, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(52, 2, 1012, 52, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(53, 2, 1013, 51, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(54, 2, 1014, 52, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(55, 2, 1015, 53, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, NOW());

-- ===== 테넌트 2 찜 =====
INSERT INTO cm_wishlist_items (id, tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(51, 2, 1016, 51, NOW() - INTERVAL 5 DAY, NOW()),
(52, 2, 1017, 52, NOW() - INTERVAL 4 DAY, NOW()),
(53, 2, 1018, 53, NOW() - INTERVAL 3 DAY, NOW()),
(54, 2, 1019, 54, NOW() - INTERVAL 2 DAY, NOW()),
(55, 2, 1020, 51, NOW() - INTERVAL 1 DAY, NOW());

-- ===== 테넌트 3 장바구니 =====
INSERT INTO cart_items (id, tenant_id, user_id, course_time_id, added_at, created_at, updated_at) VALUES
(101, 3, 2012, 101, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(102, 3, 2013, 102, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(103, 3, 2014, 103, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(104, 3, 2015, 104, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW()),
(105, 3, 2016, 101, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, NOW());

-- ===== 테넌트 3 찜 =====
INSERT INTO cm_wishlist_items (id, tenant_id, user_id, course_time_id, created_at, updated_at) VALUES
(101, 3, 2017, 101, NOW() - INTERVAL 5 DAY, NOW()),
(102, 3, 2018, 102, NOW() - INTERVAL 4 DAY, NOW()),
(103, 3, 2019, 103, NOW() - INTERVAL 3 DAY, NOW()),
(104, 3, 2020, 104, NOW() - INTERVAL 2 DAY, NOW()),
(105, 3, 2021, 101, NOW() - INTERVAL 1 DAY, NOW());
