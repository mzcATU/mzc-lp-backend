package com.mzc.lp.domain.enrollment.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.dto.request.CreateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.request.UpdateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.response.AutoEnrollmentRuleResponse;
import com.mzc.lp.domain.enrollment.service.AutoEnrollmentRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auto-enrollment-rules")
@RequiredArgsConstructor
@Validated
public class AutoEnrollmentRuleController {

    private final AutoEnrollmentRuleService autoEnrollmentRuleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AutoEnrollmentRuleResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) AutoEnrollmentTrigger trigger,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AutoEnrollmentRuleResponse> response = autoEnrollmentRuleService.getAllWithFilters(
                principal.tenantId(), keyword, isActive, trigger, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<AutoEnrollmentRuleResponse>>> getActiveRules(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<AutoEnrollmentRuleResponse> response = autoEnrollmentRuleService.getActiveRules(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trigger/{trigger}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<AutoEnrollmentRuleResponse>>> getByTrigger(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable AutoEnrollmentTrigger trigger
    ) {
        List<AutoEnrollmentRuleResponse> response = autoEnrollmentRuleService.getByTrigger(principal.tenantId(), trigger);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoEnrollmentRuleResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ruleId
    ) {
        AutoEnrollmentRuleResponse response = autoEnrollmentRuleService.getById(principal.tenantId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoEnrollmentRuleResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAutoEnrollmentRuleRequest request
    ) {
        AutoEnrollmentRuleResponse response = autoEnrollmentRuleService.create(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoEnrollmentRuleResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateAutoEnrollmentRuleRequest request
    ) {
        AutoEnrollmentRuleResponse response = autoEnrollmentRuleService.update(principal.tenantId(), ruleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ruleId
    ) {
        autoEnrollmentRuleService.delete(principal.tenantId(), ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ruleId}/activate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoEnrollmentRuleResponse>> activate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ruleId
    ) {
        AutoEnrollmentRuleResponse response = autoEnrollmentRuleService.activate(principal.tenantId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{ruleId}/deactivate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoEnrollmentRuleResponse>> deactivate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ruleId
    ) {
        AutoEnrollmentRuleResponse response = autoEnrollmentRuleService.deactivate(principal.tenantId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
