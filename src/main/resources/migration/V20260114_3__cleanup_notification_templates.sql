-- 작동하지 않는 트리거 타입의 알림 템플릿 삭제
DELETE FROM notification_templates
WHERE trigger_type NOT IN ('WELCOME', 'ENROLLMENT_COMPLETE', 'COURSE_COMPLETE');
