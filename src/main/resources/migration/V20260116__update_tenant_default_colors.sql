-- 테넌트 설정 기본 색상을 보라색으로 변경
-- 기존에 파란색(#3B82F6, #1E40AF)을 사용하던 테넌트들을 보라색(#4C2D9A, #3D2478)으로 업데이트

UPDATE tenant_settings
SET primary_color = '#4C2D9A',
    secondary_color = '#3D2478'
WHERE primary_color = '#3B82F6'
  AND secondary_color = '#1E40AF';
