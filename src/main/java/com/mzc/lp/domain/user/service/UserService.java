package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.ChangeRoleRequest;
import com.mzc.lp.domain.user.dto.request.ChangeStatusRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.CourseRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;
import com.mzc.lp.domain.user.dto.response.UserListResponse;
import com.mzc.lp.domain.user.dto.response.UserRoleResponse;
import com.mzc.lp.domain.user.dto.response.UserStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    // /me API
    UserDetailResponse getMe(Long userId);

    UserDetailResponse updateMe(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void withdraw(Long userId, WithdrawRequest request);

    // 관리 API (OPERATOR 권한)
    Page<UserListResponse> getUsers(String keyword, TenantRole role, UserStatus status, Pageable pageable);

    UserDetailResponse getUser(Long userId);

    UserRoleResponse changeUserRole(Long userId, ChangeRoleRequest request);

    UserStatusResponse changeUserStatus(Long userId, ChangeStatusRequest request);

    // CourseRole API
    CourseRoleResponse requestDesignerRole(Long userId);

    List<CourseRoleResponse> getMyCourseRoles(Long userId);
}
