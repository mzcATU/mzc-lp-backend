package com.mzc.lp.domain.user.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.dto.request.AssignCourseRoleRequest;
import com.mzc.lp.domain.user.dto.request.BulkCreateUsersRequest;
import com.mzc.lp.domain.user.dto.request.FileBulkCreateUsersRequest;
import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.ChangeRoleRequest;
import com.mzc.lp.domain.user.dto.request.ChangeStatusRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.BulkCreateUsersResponse;
import com.mzc.lp.domain.user.dto.response.CourseRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;
import com.mzc.lp.domain.user.dto.response.UserListResponse;
import com.mzc.lp.domain.user.dto.response.UserRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserRolesResponse;
import com.mzc.lp.domain.user.dto.response.UserStatusResponse;
import com.mzc.lp.domain.user.dto.response.ProfileImageResponse;
import com.mzc.lp.domain.user.service.UserService;

import java.util.List;
import java.util.Set;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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

    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam MultipartFile file
    ) {
        ProfileImageResponse response = userService.uploadProfileImage(principal.id(), file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== CourseRole API ==========

    @PostMapping("/me/course-roles/designer")
    public ResponseEntity<ApiResponse<CourseRoleResponse>> requestDesignerRole(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseRoleResponse response = userService.requestDesignerRole(principal.id());
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping("/me/course-roles")
    public ResponseEntity<ApiResponse<List<CourseRoleResponse>>> getMyCourseRoles(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CourseRoleResponse> response = userService.getMyCourseRoles(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 관리 API (OPERATOR 이상 권한) ==========

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> getUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TenantRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Boolean hasCourseRole,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TENANT_ADMIN은 자신의 테넌트 사용자만 조회 가능
        Long tenantId = principal.tenantId();
        Page<UserListResponse> response = userService.getUsers(tenantId, keyword, role, status, hasCourseRole, pageable);
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
// ========== User Roles API (1:N 역할 관리) ==========

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserRolesResponse>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        UserRolesResponse response = userService.updateUserRoles(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserRolesResponse>> addUserRole(
            @PathVariable Long userId,
            @PathVariable TenantRole role
    ) {
        UserRolesResponse response = userService.addUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserRolesResponse>> removeUserRole(
            @PathVariable Long userId,
            @PathVariable TenantRole role
    ) {
        UserRolesResponse response = userService.removeUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Set<TenantRole>>> getUserRoles(
            @PathVariable Long userId
    ) {
        Set<TenantRole> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
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

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserDetailResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ========== CourseRole 관리 API (OPERATOR 이상 권한) ==========

    @PostMapping("/{userId}/course-roles")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseRoleResponse>> assignCourseRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignCourseRoleRequest request
    ) {
        CourseRoleResponse response = userService.assignCourseRole(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{userId}/course-roles/{courseRoleId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> revokeCourseRole(
            @PathVariable Long userId,
            @PathVariable Long courseRoleId
    ) {
        userService.revokeCourseRole(userId, courseRoleId);
        return ResponseEntity.noContent().build();
    }

    // ========== 단체 계정 생성 API (TENANT_ADMIN 권한) ==========

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BulkCreateUsersResponse>> bulkCreateUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BulkCreateUsersRequest request
    ) {
        BulkCreateUsersResponse response = userService.bulkCreateUsers(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PostMapping(value = "/bulk/file", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BulkCreateUsersResponse>> fileBulkCreateUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "defaultPassword", required = false) String defaultPassword,
            @RequestParam(value = "role", required = false) TenantRole role,
            @RequestParam(value = "autoLinkEmployees", required = false) Boolean autoLinkEmployees,
            @RequestParam(value = "sendWelcomeEmail", required = false) Boolean sendWelcomeEmail
    ) {
        log.info("fileBulkCreateUsers called: fileName={}, fileSize={}, contentType={}, defaultPassword={}, role={}, autoLinkEmployees={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType(),
                defaultPassword != null ? "***" : null, role, autoLinkEmployees);

        FileBulkCreateUsersRequest request = new FileBulkCreateUsersRequest(
                defaultPassword, role, autoLinkEmployees, sendWelcomeEmail);
        BulkCreateUsersResponse response = userService.fileBulkCreateUsers(principal.tenantId(), file, request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }
}
