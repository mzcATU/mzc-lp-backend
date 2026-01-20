package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.sa.dto.request.CreateSystemAdminRequest;
import com.mzc.lp.domain.sa.dto.response.SystemAdminUserResponse;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.DuplicateEmailException;
import com.mzc.lp.domain.user.repository.UserRepository;
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
public class SaUserServiceImpl implements SaUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SystemAdminUserResponse> getSystemAdmins(String keyword, UserStatus status, Pageable pageable) {
        log.debug("Getting system admin users: keyword={}, status={}", keyword, status);

        Page<User> users = userRepository.searchSystemAdmins(keyword, status, pageable);

        return users.map(SystemAdminUserResponse::from);
    }

    @Override
    @Transactional
    public SystemAdminUserResponse createSystemAdmin(CreateSystemAdminRequest request) {
        log.info("Creating system admin: email={}, name={}", request.email(), request.name());

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // SYSTEM_ADMIN 사용자 생성 (tenantId 없음)
        User user = User.create(
                request.email(),
                request.name(),
                passwordEncoder.encode(request.password()),
                request.phone()
        );

        // 부서, 직책 설정
        if (request.department() != null || request.position() != null) {
            user.updateProfile(
                    user.getName(),
                    user.getPhone(),
                    user.getProfileImageUrl(),
                    request.department(),
                    request.position()
            );
        }

        // SYSTEM_ADMIN 역할 부여
        user.updateRole(TenantRole.SYSTEM_ADMIN);

        User savedUser = userRepository.save(user);

        log.info("System admin created: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return SystemAdminUserResponse.from(savedUser);
    }
}
