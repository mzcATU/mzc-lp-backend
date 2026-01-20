-- =============================================
-- V009: SYSTEM_ADMIN 사용자의 tenant_id를 NULL로 업데이트
-- =============================================
-- SYSTEM_ADMIN은 특정 테넌트에 속하지 않으므로 tenant_id가 NULL이어야 함
-- 다른 역할은 반드시 tenant_id를 가져야 함

-- 1. 먼저 users 테이블의 tenant_id 컬럼을 nullable로 변경
ALTER TABLE users MODIFY COLUMN tenant_id BIGINT NULL;

-- 2. SYSTEM_ADMIN 역할을 가진 사용자의 tenant_id를 NULL로 업데이트
UPDATE users
SET tenant_id = NULL
WHERE role = 'SYSTEM_ADMIN';

-- 3. SYSTEM_ADMIN이 아닌 사용자는 반드시 tenant_id를 가져야 한다는 체크 제약조건 추가
ALTER TABLE users
ADD CONSTRAINT chk_tenant_id_required_unless_system_admin
CHECK (role = 'SYSTEM_ADMIN' OR tenant_id IS NOT NULL);
