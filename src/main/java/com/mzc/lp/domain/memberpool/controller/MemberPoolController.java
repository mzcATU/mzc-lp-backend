package com.mzc.lp.domain.memberpool.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.memberpool.dto.request.CreateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.request.MemberPoolConditionDto;
import com.mzc.lp.domain.memberpool.dto.request.UpdateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolMemberResponse;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolResponse;
import com.mzc.lp.domain.memberpool.service.MemberPoolService;
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
@RequestMapping("/api/member-pools")
@RequiredArgsConstructor
@Validated
public class MemberPoolController {

    private final MemberPoolService memberPoolService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<MemberPoolResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<MemberPoolResponse> response = memberPoolService.getAll(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<MemberPoolResponse>>> getActivePools(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<MemberPoolResponse> response = memberPoolService.getActivePoolls(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{poolId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPoolResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId
    ) {
        MemberPoolResponse response = memberPoolService.getById(principal.tenantId(), poolId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{poolId}/members")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<MemberPoolMemberResponse>>> getMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MemberPoolMemberResponse> response = memberPoolService.getMembers(principal.tenantId(), poolId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<MemberPoolMemberResponse>>> previewMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MemberPoolConditionDto conditions,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MemberPoolMemberResponse> response = memberPoolService.previewMembers(principal.tenantId(), conditions, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPoolResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateMemberPoolRequest request
    ) {
        MemberPoolResponse response = memberPoolService.create(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PutMapping("/{poolId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPoolResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId,
            @Valid @RequestBody UpdateMemberPoolRequest request
    ) {
        MemberPoolResponse response = memberPoolService.update(principal.tenantId(), poolId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{poolId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId
    ) {
        memberPoolService.delete(principal.tenantId(), poolId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{poolId}/activate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPoolResponse>> activate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId
    ) {
        MemberPoolResponse response = memberPoolService.activate(principal.tenantId(), poolId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{poolId}/deactivate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPoolResponse>> deactivate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long poolId
    ) {
        MemberPoolResponse response = memberPoolService.deactivate(principal.tenantId(), poolId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
