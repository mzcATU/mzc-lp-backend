package com.mzc.lp.domain.notification.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import com.mzc.lp.domain.notification.dto.request.CreateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.request.UpdateNotificationTemplateRequest;
import com.mzc.lp.domain.notification.dto.response.NotificationTemplateResponse;
import com.mzc.lp.domain.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 알림 템플릿 관리 API (TA - Tenant Admin)
 */
@RestController
@RequestMapping("/api/ta/notification-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    /**
     * 템플릿 목록 조회
     */
    @GetMapping
    public ApiResponse<List<NotificationTemplateResponse>> getTemplates(
            @RequestParam(required = false) NotificationCategory category
    ) {
        List<NotificationTemplateResponse> templates = templateService.getTemplates(category);
        return ApiResponse.success(templates);
    }

    /**
     * 템플릿 상세 조회
     */
    @GetMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> getTemplate(@PathVariable Long id) {
        NotificationTemplateResponse template = templateService.getTemplate(id);
        return ApiResponse.success(template);
    }

    /**
     * 템플릿 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request
    ) {
        NotificationTemplateResponse template = templateService.createTemplate(request);
        return ApiResponse.success(template);
    }

    /**
     * 기본 템플릿 초기화
     */
    @PostMapping("/initialize")
    public ApiResponse<Void> initializeDefaultTemplates() {
        templateService.initializeDefaultTemplates();
        return ApiResponse.success(null);
    }

    /**
     * 템플릿 수정
     */
    @PutMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationTemplateRequest request
    ) {
        NotificationTemplateResponse template = templateService.updateTemplate(id, request);
        return ApiResponse.success(template);
    }

    /**
     * 템플릿 활성화
     */
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activateTemplate(@PathVariable Long id) {
        templateService.activateTemplate(id);
        return ApiResponse.success(null);
    }

    /**
     * 템플릿 비활성화
     */
    @PostMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivateTemplate(@PathVariable Long id) {
        templateService.deactivateTemplate(id);
        return ApiResponse.success(null);
    }

    /**
     * 템플릿 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    /**
     * 사용 가능한 트리거 타입 목록 조회
     */
    @GetMapping("/triggers")
    public ApiResponse<List<Map<String, String>>> getTriggerTypes() {
        List<Map<String, String>> triggers = Arrays.stream(NotificationTrigger.values())
                .map(t -> Map.of(
                        "value", t.name(),
                        "label", t.getDisplayName(),
                        "category", t.getCategory().name(),
                        "categoryLabel", t.getCategory().getDisplayName()
                ))
                .toList();
        return ApiResponse.success(triggers);
    }

    /**
     * 사용 가능한 카테고리 목록 조회
     */
    @GetMapping("/categories")
    public ApiResponse<List<Map<String, String>>> getCategories() {
        List<Map<String, String>> categories = Arrays.stream(NotificationCategory.values())
                .map(c -> Map.of(
                        "value", c.name(),
                        "label", c.getDisplayName()
                ))
                .toList();
        return ApiResponse.success(categories);
    }
}
