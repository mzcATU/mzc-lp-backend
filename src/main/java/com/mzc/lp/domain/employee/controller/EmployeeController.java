package com.mzc.lp.domain.employee.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.dto.request.ChangeEmployeeStatusRequest;
import com.mzc.lp.domain.employee.dto.request.CreateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.request.UpdateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.response.EmployeeResponse;
import com.mzc.lp.domain.employee.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EmployeeResponse> response = employeeService.getAll(principal.tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EmployeeResponse> response = employeeService.search(
                principal.tenantId(), departmentId, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getByDepartment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long departmentId
    ) {
        List<EmployeeResponse> response = employeeService.getByDepartment(
                principal.tenantId(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{employeeNumber}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getByEmployeeNumber(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String employeeNumber
    ) {
        EmployeeResponse response = employeeService.getByEmployeeNumber(
                principal.tenantId(), employeeNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long employeeId
    ) {
        EmployeeResponse response = employeeService.getById(principal.tenantId(), employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.create(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.update(principal.tenantId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{employeeId}/status")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> changeStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long employeeId,
            @Valid @RequestBody ChangeEmployeeStatusRequest request
    ) {
        EmployeeResponse response = employeeService.changeStatus(
                principal.tenantId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long employeeId
    ) {
        employeeService.delete(principal.tenantId(), employeeId);
        return ResponseEntity.noContent().build();
    }
}
