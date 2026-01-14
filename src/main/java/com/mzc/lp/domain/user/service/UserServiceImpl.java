package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.user.dto.request.AssignCourseRoleRequest;
import com.mzc.lp.domain.user.dto.request.BulkCreateUsersRequest;
import com.mzc.lp.domain.user.dto.request.FileBulkCreateUsersRequest;
import com.mzc.lp.domain.user.dto.request.FileParseResult;
import com.mzc.lp.domain.user.dto.request.FileUserRow;
import com.mzc.lp.domain.user.util.UserExcelParser;
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
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.exception.CourseRoleNotFoundException;
import com.mzc.lp.domain.user.exception.PasswordMismatchException;
import com.mzc.lp.domain.user.exception.RoleAlreadyExistsException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import com.mzc.lp.common.service.FileStorageService;
import com.mzc.lp.domain.employee.entity.Employee;
import com.mzc.lp.domain.employee.repository.EmployeeRepository;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.repository.TenantRepository;

import com.mzc.lp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mzc.lp.domain.user.dto.response.UserRolesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileStorageService fileStorageService;
    private final UserExcelParser userExcelParser;
    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;

    @Override
    public UserDetailResponse getMe(Long userId) {
        log.debug("Getting user info: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<CourseRoleResponse> courseRoles = getCourseRolesWithProgramTitle(userId);

        // 테넌트 정보 조회 (subdomain, customDomain)
        String tenantSubdomain = null;
        String tenantCustomDomain = null;
        if (user.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
            if (tenant != null) {
                tenantSubdomain = tenant.getSubdomain();
                tenantCustomDomain = tenant.getCustomDomain();
            }
        }

        return UserDetailResponse.from(user, courseRoles, tenantSubdomain, tenantCustomDomain);
    }

    @Override
    @Transactional
    public UserDetailResponse updateMe(Long userId, UpdateProfileRequest request) {
        log.info("Updating user profile: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        // null이 전달되면 기존 값 유지
        String name = request.name() != null ? request.name() : user.getName();
        String phone = request.phone() != null ? request.phone() : user.getPhone();
        String profileImageUrl = request.profileImageUrl() != null ? request.profileImageUrl() : user.getProfileImageUrl();
        String department = request.department() != null ? request.department() : user.getDepartment();
        String position = request.position() != null ? request.position() : user.getPosition();
        user.updateProfile(name, phone, profileImageUrl, department, position);
        List<CourseRoleResponse> courseRoles = getCourseRolesWithProgramTitle(userId);
        return UserDetailResponse.from(user, courseRoles);
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
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User withdrawn: userId={}, reason={}", userId, request.reason());
    }

    @Override
    @Transactional
    public ProfileImageResponse uploadProfileImage(Long userId, MultipartFile file) {
        log.info("Uploading profile image: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String profileImageUrl = fileStorageService.storeProfileImage(file);
        user.updateProfile(user.getName(), user.getPhone(), profileImageUrl);

        log.info("Profile image uploaded: userId={}, url={}", userId, profileImageUrl);
        return ProfileImageResponse.from(profileImageUrl);
    }

    // ========== 관리 API (OPERATOR 권한) ==========

    @Override
    public Page<UserListResponse> getUsers(Long tenantId, String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole, Pageable pageable) {
        log.debug("Searching users: tenantId={}, keyword={}, role={}, status={}, hasCourseRole={}", tenantId, keyword, role, status, hasCourseRole);
        return userRepository.searchUsers(tenantId, keyword, role, status, hasCourseRole, pageable)
                .map(UserListResponse::from);
    }

    @Override
    public UserDetailResponse getUser(Long userId) {
        log.debug("Getting user detail: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<CourseRoleResponse> courseRoles = getCourseRolesWithProgramTitle(userId);
        return UserDetailResponse.from(user, courseRoles);
    }
    @Override
    @Transactional
    public UserDetailResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user by admin: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.name() != null || request.phone() != null) {
            user.updateProfile(
                    request.name() != null ? request.name() : user.getName(),
                    request.phone() != null ? request.phone() : user.getPhone(),
                    user.getProfileImageUrl()
            );
        }

        if (request.role() != null) {
            user.updateRole(request.role());
        }

        if (request.status() != null) {
            switch (request.status()) {
                case ACTIVE -> user.activate();
                case SUSPENDED -> user.suspend();
                case WITHDRAWN -> user.withdraw();
                default -> throw new IllegalArgumentException("Unknown status: " + request.status());
            }
        }

        log.info("User updated by admin: userId={}", userId);
        List<CourseRoleResponse> courseRoles = getCourseRolesWithProgramTitle(userId);
        return UserDetailResponse.from(user, courseRoles);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user by admin: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        refreshTokenRepository.deleteByUserId(userId);
        userRepository.delete(user);

        log.info("User deleted by admin: userId={}", userId);
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

    // ========== CourseRole 관리 API (OPERATOR 권한) ==========

    @Override
    @Transactional
    public CourseRoleResponse assignCourseRole(Long userId, AssignCourseRoleRequest request) {
        log.info("Assigning course role: userId={}, courseId={}, role={}", userId, request.courseId(), request.role());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 중복 역할 검증
        if (request.courseId() == null) {
            // 테넌트 레벨 역할 (DESIGNER)
            if (userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(userId, request.role())) {
                throw new RoleAlreadyExistsException(request.role().name());
            }
        } else {
            // 강의 레벨 역할 (DESIGNER, INSTRUCTOR)
            if (userCourseRoleRepository.existsByUserIdAndCourseIdAndRole(userId, request.courseId(), request.role())) {
                throw new RoleAlreadyExistsException(request.role().name());
            }
        }

        UserCourseRole courseRole = UserCourseRole.create(user, request.courseId(), request.role(), request.revenueSharePercent());
        UserCourseRole savedRole = userCourseRoleRepository.save(courseRole);
        log.info("Course role assigned: userId={}, courseRoleId={}, role={}", userId, savedRole.getId(), request.role());

        return CourseRoleResponse.from(savedRole);
    }

    @Override
    @Transactional
    public void revokeCourseRole(Long userId, Long courseRoleId) {
        log.info("Revoking course role: userId={}, courseRoleId={}", userId, courseRoleId);

        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        UserCourseRole courseRole = userCourseRoleRepository.findById(courseRoleId)
                .orElseThrow(() -> new CourseRoleNotFoundException(courseRoleId));

        // 해당 사용자의 역할인지 확인
        if (!courseRole.getUser().getId().equals(userId)) {
            throw new CourseRoleNotFoundException(courseRoleId);
        }

        userCourseRoleRepository.delete(courseRole);
        log.info("Course role revoked: userId={}, courseRoleId={}", userId, courseRoleId);
    }

    // ========== 단체 계정 생성 API (TENANT_ADMIN 권한) ==========

    @Override
    @Transactional
    public BulkCreateUsersResponse bulkCreateUsers(Long tenantId, BulkCreateUsersRequest request) {
        log.info("Bulk creating users: tenantId={}, prefix={}, count={}", tenantId, request.emailPrefix(), request.count());

        List<BulkCreateUsersResponse.CreatedUserInfo> createdUsers = new ArrayList<>();
        List<BulkCreateUsersResponse.FailedUserInfo> failedUsers = new ArrayList<>();

        String encodedPassword = passwordEncoder.encode(request.password());

        // TenantContext 설정 (User 엔티티 저장 시 tenantId 자동 설정)
        TenantContext.setTenantId(tenantId);

        try {
            for (int i = 0; i < request.count(); i++) {
                int number = request.startNumber() + i;
                String email = request.emailPrefix() + number + request.emailDomain();
                String name = request.emailPrefix() + number;

                try {
                    // 이메일 중복 체크
                    if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
                        failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(email, "이미 존재하는 이메일입니다"));
                        continue;
                    }

                    User user = User.create(email, name, encodedPassword);
                    User savedUser = userRepository.save(user);

                    createdUsers.add(new BulkCreateUsersResponse.CreatedUserInfo(
                            savedUser.getId(),
                            savedUser.getEmail(),
                            savedUser.getName()
                    ));
                } catch (Exception e) {
                    log.warn("Failed to create user: email={}, error={}", email, e.getMessage());
                    failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(email, e.getMessage()));
                }
            }
        } finally {
            TenantContext.clear();
        }

        log.info("Bulk user creation completed: created={}, failed={}", createdUsers.size(), failedUsers.size());
        return BulkCreateUsersResponse.of(request.count(), createdUsers, failedUsers);
    }

    @Override
    @Transactional
    public BulkCreateUsersResponse fileBulkCreateUsers(Long tenantId, MultipartFile file, FileBulkCreateUsersRequest request) {
        log.info("File bulk creating users: tenantId={}, fileName={}, autoLinkEmployees={}",
                tenantId, file.getOriginalFilename(), request.autoLinkEmployees());

        List<BulkCreateUsersResponse.CreatedUserInfo> createdUsers = new ArrayList<>();
        List<BulkCreateUsersResponse.FailedUserInfo> failedUsers = new ArrayList<>();
        List<BulkCreateUsersResponse.AutoLinkedUserInfo> autoLinkedUsers = new ArrayList<>();

        // 파일 파싱 (헤더 기반 매핑 + 검증)
        FileParseResult parseResult;
        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                parseResult = userExcelParser.parseCsvWithValidation(file);
            } else {
                parseResult = userExcelParser.parseExcelWithValidation(file);
            }
        } catch (Exception e) {
            log.error("Failed to parse file: {}", e.getMessage(), e);
            throw new IllegalArgumentException("파일 파싱에 실패했습니다: " + e.getMessage());
        }

        // 헤더 에러인 경우 즉시 반환
        if (parseResult.hasHeaderError()) {
            String errorMessage = parseResult.errors().isEmpty() ? "헤더 오류"
                    : parseResult.errors().get(0).message();
            throw new IllegalArgumentException(errorMessage);
        }

        // 파싱 에러를 failedUsers에 추가
        for (FileParseResult.ParseError error : parseResult.errors()) {
            failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(
                    error.value().isEmpty() ? "(행 " + error.rowNumber() + ")" : error.value(),
                    error.message(),
                    error.rowNumber()
            ));
        }

        List<FileUserRow> userRows = parseResult.users();
        if (userRows.isEmpty() && failedUsers.isEmpty()) {
            throw new IllegalArgumentException("파일에서 유효한 사용자 데이터를 찾을 수 없습니다");
        }

        // 자동 연동을 위한 임직원 이메일 매핑 조회
        Map<String, Employee> employeeEmailMap = Map.of();
        if (Boolean.TRUE.equals(request.autoLinkEmployees())) {
            List<String> emails = userRows.stream()
                    .map(FileUserRow::email)
                    .filter(email -> email != null && !email.isBlank())
                    .toList();

            List<Employee> matchedEmployees = employeeRepository.findByTenantIdAndUserEmailIn(tenantId, emails);
            employeeEmailMap = matchedEmployees.stream()
                    .collect(Collectors.toMap(
                            e -> e.getUser().getEmail().toLowerCase(),
                            e -> e,
                            (e1, e2) -> e1
                    ));
            log.info("Found {} employees matching uploaded emails", matchedEmployees.size());
        }

        TenantContext.setTenantId(tenantId);

        try {
            for (FileUserRow row : userRows) {
                try {
                    // 이메일 중복 체크
                    if (userRepository.existsByTenantIdAndEmail(tenantId, row.email())) {
                        failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(row.email(), "이미 존재하는 이메일입니다"));
                        continue;
                    }

                    // 비밀번호 결정 (파일에 있으면 사용, 없으면 기본값)
                    String password = row.password() != null && !row.password().isBlank()
                            ? row.password()
                            : request.defaultPassword();

                    if (password == null || password.isBlank()) {
                        failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(row.email(), "비밀번호가 지정되지 않았습니다"));
                        continue;
                    }

                    // 이름 결정 (파일에 있으면 사용, 없으면 이메일 앞부분)
                    String name = row.name() != null && !row.name().isBlank()
                            ? row.name()
                            : row.email().split("@")[0];

                    String encodedPassword = passwordEncoder.encode(password);
                    User user = User.create(row.email(), name, encodedPassword, row.phone());

                    // 부서 및 직급 설정
                    if ((row.department() != null && !row.department().isBlank()) ||
                        (row.position() != null && !row.position().isBlank())) {
                        user.updateProfile(name, row.phone(), null, row.department(), row.position());
                    }

                    // 역할 설정
                    if (request.role() != null) {
                        user.updateRole(request.role());
                    }

                    User savedUser = userRepository.save(user);

                    // 임직원 자동 연동 체크
                    Employee matchedEmployee = employeeEmailMap.get(row.email().toLowerCase());
                    boolean employeeLinked = matchedEmployee != null;
                    Long employeeId = employeeLinked ? matchedEmployee.getId() : null;

                    createdUsers.add(new BulkCreateUsersResponse.CreatedUserInfo(
                            savedUser.getId(),
                            savedUser.getEmail(),
                            savedUser.getName(),
                            employeeLinked,
                            employeeId
                    ));

                    // 자동 연동된 임직원 정보 추가
                    if (employeeLinked) {
                        String departmentName = matchedEmployee.getDepartment() != null
                                ? matchedEmployee.getDepartment().getName()
                                : null;

                        autoLinkedUsers.add(new BulkCreateUsersResponse.AutoLinkedUserInfo(
                                savedUser.getId(),
                                savedUser.getEmail(),
                                matchedEmployee.getId(),
                                matchedEmployee.getEmployeeNumber(),
                                matchedEmployee.getUser().getName(),
                                departmentName,
                                matchedEmployee.getPosition(),
                                matchedEmployee.getJobTitle()
                        ));

                        log.debug("Auto-linked user {} with employee {}", savedUser.getEmail(), matchedEmployee.getEmployeeNumber());
                    }
                } catch (Exception e) {
                    log.warn("Failed to create user: email={}, error={}", row.email(), e.getMessage());
                    failedUsers.add(new BulkCreateUsersResponse.FailedUserInfo(row.email(), e.getMessage()));
                }
            }
        } finally {
            TenantContext.clear();
        }

        log.info("File bulk user creation completed: created={}, failed={}, autoLinked={}",
                createdUsers.size(), failedUsers.size(), autoLinkedUsers.size());
        return BulkCreateUsersResponse.of(userRows.size(), createdUsers, failedUsers, autoLinkedUsers);
    }

    // ========== User Roles API (1:N 역할 관리) ==========

    @Override
    @Transactional
    public UserRolesResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        log.info("Updating user roles: userId={}, roles={}", userId, request.roles());
        // userRoles를 함께 로딩해서 기존 역할과 비교 가능하게 함
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setRoles(request.roles());
        log.info("User roles updated: userId={}, newRoles={}", userId, user.getRoles());

        return UserRolesResponse.from(user);
    }

    @Override
    @Transactional
    public UserRolesResponse addUserRole(Long userId, TenantRole role) {
        log.info("Adding user role: userId={}, role={}", userId, role);
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.addRole(role);
        log.info("User role added: userId={}, allRoles={}", userId, user.getRoles());

        return UserRolesResponse.from(user);
    }

    @Override
    @Transactional
    public UserRolesResponse removeUserRole(Long userId, TenantRole role) {
        log.info("Removing user role: userId={}, role={}", userId, role);
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.removeRole(role);
        log.info("User role removed: userId={}, remainingRoles={}", userId, user.getRoles());

        return UserRolesResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<TenantRole> getUserRoles(Long userId) {
        log.debug("Getting user roles: userId={}", userId);
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getRoles();
    }

    // ========== Private Helper Methods ==========

    /**
     * 사용자의 CourseRole 목록을 Program title과 함께 조회
     */
    private List<CourseRoleResponse> getCourseRolesWithProgramTitle(Long userId) {
        return userCourseRoleRepository.findByUserIdWithProgramTitle(userId).stream()
                .map(row -> {
                    UserCourseRole ucr = (UserCourseRole) row[0];
                    String programTitle = (String) row[1];
                    return CourseRoleResponse.from(ucr, programTitle);
                })
                .toList();
    }
}
