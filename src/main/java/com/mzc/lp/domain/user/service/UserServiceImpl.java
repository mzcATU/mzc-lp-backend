package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.constant.CourseRole;
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
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.exception.PasswordMismatchException;
import com.mzc.lp.domain.user.exception.RoleAlreadyExistsException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailResponse getMe(Long userId) {
        log.debug("Getting user info: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserDetailResponse.from(user);
    }

    @Override
    @Transactional
    public UserDetailResponse updateMe(Long userId, UpdateProfileRequest request) {
        log.info("Updating user profile: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateProfile(request.name(), request.phone(), request.profileImageUrl());
        return UserDetailResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed: userId={}", userId);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        log.info("Withdrawing user: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new PasswordMismatchException();
        }

        user.withdraw();
        log.info("User withdrawn: userId={}", userId);
    }

    // ========== 관리 API (OPERATOR 권한) ==========

    @Override
    public Page<UserListResponse> getUsers(String keyword, TenantRole role, UserStatus status, Pageable pageable) {
        log.debug("Searching users: keyword={}, role={}, status={}", keyword, role, status);
        return userRepository.searchUsers(keyword, role, status, pageable)
                .map(UserListResponse::from);
    }

    @Override
    public UserDetailResponse getUser(Long userId) {
        log.debug("Getting user detail: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserDetailResponse.from(user);
    }

    @Override
    @Transactional
    public UserRoleResponse changeUserRole(Long userId, ChangeRoleRequest request) {
        log.info("Changing user role: userId={}, newRole={}", userId, request.role());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateRole(request.role());
        log.info("User role changed: userId={}, role={}", userId, request.role());
        return UserRoleResponse.from(user);
    }

    @Override
    @Transactional
    public UserStatusResponse changeUserStatus(Long userId, ChangeStatusRequest request) {
        log.info("Changing user status: userId={}, newStatus={}, reason={}", userId, request.status(), request.reason());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        switch (request.status()) {
            case ACTIVE -> user.activate();
            case SUSPENDED -> user.suspend();
            case WITHDRAWN -> user.withdraw();
            default -> throw new IllegalArgumentException("Invalid status: " + request.status());
        }

        log.info("User status changed: userId={}, status={}", userId, request.status());
        return UserStatusResponse.from(user);
    }

    // ========== CourseRole API ==========

    @Override
    @Transactional
    public CourseRoleResponse requestDesignerRole(Long userId) {
        log.info("Requesting DESIGNER role: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 이미 DESIGNER 역할이 있는지 확인
        if (userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(userId, CourseRole.DESIGNER)) {
            throw new RoleAlreadyExistsException("DESIGNER");
        }

        UserCourseRole courseRole = UserCourseRole.createDesigner(user);
        UserCourseRole savedRole = userCourseRoleRepository.save(courseRole);
        log.info("DESIGNER role granted: userId={}, courseRoleId={}", userId, savedRole.getId());

        return CourseRoleResponse.from(savedRole);
    }

    @Override
    public List<CourseRoleResponse> getMyCourseRoles(Long userId) {
        log.debug("Getting course roles: userId={}", userId);
        return userCourseRoleRepository.findByUserId(userId).stream()
                .map(CourseRoleResponse::from)
                .toList();
    }
}
