package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.dto.request.AssignCourseRoleRequest;
import com.mzc.lp.domain.user.dto.request.BulkCreateUsersRequest;
import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.ChangeRoleRequest;
import com.mzc.lp.domain.user.dto.request.ChangeStatusRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.BulkCreateUsersResponse;
import com.mzc.lp.domain.user.dto.response.CourseRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;
import com.mzc.lp.domain.user.dto.response.UserListResponse;
import com.mzc.lp.domain.user.dto.response.UserRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserStatusResponse;
import com.mzc.lp.domain.user.dto.response.ProfileImageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    // /me API
    UserDetailResponse getMe(Long userId);

    UserDetailResponse updateMe(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void withdraw(Long userId, WithdrawRequest request);

    ProfileImageResponse uploadProfileImage(Long userId, MultipartFile file);

    // 관리 API (OPERATOR 권한) - tenantId로 필터링
    Page<UserListResponse> getUsers(Long tenantId, String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole, Pageable pageable);

    UserDetailResponse getUser(Long userId);

    UserDetailResponse updateUser(Long userId, UpdateUserRequest request);

    void deleteUser(Long userId);

    UserRoleResponse changeUserRole(Long userId, ChangeRoleRequest request);

    UserStatusResponse changeUserStatus(Long userId, ChangeStatusRequest request);

    // CourseRole API
    CourseRoleResponse requestDesignerRole(Long userId);

    List<CourseRoleResponse> getMyCourseRoles(Long userId);

    // CourseRole 관리 API (OPERATOR 권한)
    CourseRoleResponse assignCourseRole(Long userId, AssignCourseRoleRequest request);

    void revokeCourseRole(Long userId, Long courseRoleId);

    // 단체 계정 생성 API (TENANT_ADMIN 권한)
    BulkCreateUsersResponse bulkCreateUsers(Long tenantId, BulkCreateUsersRequest request);
}
