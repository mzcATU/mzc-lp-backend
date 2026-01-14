package com.mzc.lp.domain.notification.service;

import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import com.mzc.lp.domain.notification.dto.request.CreateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.request.UpdateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.response.NotificationTemplateResponse;

import java.util.List;
import java.util.Map;

public interface NotificationTemplateService {

    /**
     * 템플릿 생성
     */
    NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request);

    /**
     * 기본 템플릿 초기화 (테넌트 첫 설정 시)
     */
    void initializeDefaultTemplates();

    /**
     * 템플릿 목록 조회
     */
    List<NotificationTemplateResponse> getTemplates(NotificationCategory category);

    /**
     * 템플릿 상세 조회
     */
    NotificationTemplateResponse getTemplate(Long id);

    /**
     * 트리거 타입으로 템플릿 조회
     */
    NotificationTemplateResponse getTemplateByTrigger(NotificationTrigger triggerType);

    /**
     * 템플릿 수정
     */
    NotificationTemplateResponse updateTemplate(Long id, UpdateNotificationTemplateRequest request);

    /**
     * 템플릿 활성화
     */
    void activateTemplate(Long id);

    /**
     * 템플릿 비활성화
     */
    void deactivateTemplate(Long id);

    /**
     * 템플릿 삭제
     */
    void deleteTemplate(Long id);

    /**
     * 템플릿 복제
     */
    NotificationTemplateResponse duplicateTemplate(Long id);

    /**
     * 트리거에 따른 알림 발송 (템플릿 변수 치환)
     */
    void sendNotificationByTrigger(
            NotificationTrigger trigger,
            Long userId,
            Map<String, String> variables,
            String link,
            Long referenceId,
            String referenceType
    );
}
