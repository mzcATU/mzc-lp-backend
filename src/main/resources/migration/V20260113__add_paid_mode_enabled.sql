-- 테넌트 설정에 유료 모드 필드 추가
ALTER TABLE tenant_settings
ADD COLUMN paid_mode_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- 기존 테넌트는 기본값 유료 모드로 설정
UPDATE tenant_settings SET paid_mode_enabled = TRUE WHERE paid_mode_enabled IS NULL;
