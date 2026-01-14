-- 사용자 역할 테이블 (1:N 관계)
-- 한 사용자가 여러 역할을 가질 수 있도록 지원

CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role)
);

-- 인덱스 추가
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- 기존 사용자의 역할을 user_roles 테이블로 마이그레이션
INSERT INTO user_roles (user_id, role, created_at)
SELECT id, role, NOW()
FROM users
WHERE role IS NOT NULL;
