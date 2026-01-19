package com.mzc.lp.domain.department.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.department.dto.request.CreateDepartmentRequest;
import com.mzc.lp.domain.department.dto.request.UpdateDepartmentRequest;
import com.mzc.lp.domain.department.dto.response.DepartmentMemberResponse;
import com.mzc.lp.domain.department.dto.response.DepartmentResponse;
import com.mzc.lp.domain.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DepartmentResponse> response = departmentService.getAll(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getTree(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DepartmentResponse> response = departmentService.getRootDepartments(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getActiveDepartments(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DepartmentResponse> response = departmentService.getActiveDepartments(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String keyword
    ) {
        List<DepartmentResponse> response = departmentService.search(principal.tenantId(), keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId
    ) {
        DepartmentResponse response = departmentService.getById(principal.tenantId(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{departmentId}/members")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentMemberResponse>>> getMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId
    ) {
        List<DepartmentMemberResponse> response = departmentService.getMembersByDepartmentId(principal.tenantId(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{departmentId}/available-members")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentMemberResponse>>> getAvailableMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId
    ) {
        List<DepartmentMemberResponse> response = departmentService.getAvailableMembersForDepartment(principal.tenantId(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{departmentId}/members/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> addMemberToDepartment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId,
            @PathVariable Long userId
    ) {
        departmentService.addMemberToDepartment(principal.tenantId(), departmentId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateDepartmentRequest request
    ) {
        DepartmentResponse response = departmentService.create(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        DepartmentResponse response = departmentService.update(principal.tenantId(), departmentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId
    ) {
        departmentService.delete(principal.tenantId(), departmentId);
        return ResponseEntity.noContent().build();
    }
}
