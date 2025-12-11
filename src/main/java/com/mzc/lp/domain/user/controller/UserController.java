package com.mzc.lp.domain.user.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.ChangeRoleRequest;
import com.mzc.lp.domain.user.dto.request.ChangeStatusRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;
import com.mzc.lp.domain.user.dto.response.UserListResponse;
import com.mzc.lp.domain.user.dto.response.UserRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserStatusResponse;
import com.mzc.lp.domain.user.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getMe(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDetailResponse response = userService.getMe(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserDetailResponse response = userService.updateMe(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WithdrawRequest request
    ) {
        userService.withdraw(principal.id(), request);
        return ResponseEntity.noContent().build();
    }

    // ========== 관리 API (OPERATOR 이상 권한) ==========

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TenantRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserListResponse> response = userService.getUsers(keyword, role, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(
            @PathVariable Long userId
    ) {
        UserDetailResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserRoleResponse>> changeUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        UserRoleResponse response = userService.changeUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserStatusResponse>> changeUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        UserStatusResponse response = userService.changeUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
