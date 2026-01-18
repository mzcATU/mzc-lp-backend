package com.mzc.lp.domain.user.service;

import com.mzc.lp.common.security.JwtProvider;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.service.ActivityLogService;
import com.mzc.lp.domain.notification.event.NotificationEventPublisher;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RefreshTokenRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.dto.request.SwitchRoleRequest;
import com.mzc.lp.domain.user.dto.response.TokenResponse;
import com.mzc.lp.domain.user.dto.response.UserResponse;
import com.mzc.lp.domain.user.entity.RefreshToken;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.exception.DuplicateEmailException;
import com.mzc.lp.domain.user.exception.InvalidCredentialsException;
import com.mzc.lp.domain.user.exception.InvalidTokenException;
import com.mzc.lp.domain.user.exception.RoleNotAssignedException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final NotificationEventPublisher notificationEventPublisher;
    private final ActivityLogService activityLogService;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiry;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 비밀번호 암호화 및 User 생성
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
                request.email(),
                request.name(),
                encodedPassword,
                request.phone()
        );

        // 저장
        User savedUser = userRepository.save(user);
        log.info("User registered: userId={}", savedUser.getId());

        // 회원가입 환영 알림 발송
        notificationEventPublisher.publishWelcome(
                savedUser.getTenantId(),
                savedUser.getId(),
                savedUser.getName()
        );

        return UserResponse.from(savedUser);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.email());

        // 사용자 조회 (userRoles를 함께 로딩 - 다중 역할 지원)
        // 로그인 시에는 TenantContext가 설정되지 않으므로 테넌트 필터가 적용되지 않음
        User user = userRepository.findByEmailWithRoles(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email={}", request.email());
                    return new InvalidCredentialsException();
                });

        log.debug("User found: userId={}, tenantId={}", user.getId(), user.getTenantId());

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: password mismatch for email={}", request.email());
            throw new InvalidCredentialsException();
        }

        // 탈퇴/정지 사용자 체크
        if (user.getStatus() == UserStatus.WITHDRAWN || user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidCredentialsException();
        }

        // 토큰 생성 (tenantId, 다중 역할 포함)
        Set<String> roleNames = user.getRoles().stream()
                .map(TenantRole::name)
                .collect(Collectors.toSet());
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                roleNames,
                user.getTenantId()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
                refreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiry()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in: userId={}", user.getId());

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpiry);
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(request.refreshToken())) {
            throw new InvalidTokenException();
        }

        // DB에서 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(InvalidTokenException::new);

        if (!storedToken.isValid()) {
            throw new InvalidTokenException();
        }

        // 사용자 조회 (userRoles를 함께 로딩 - 다중 역할 지원)
        User user = userRepository.findByIdWithRoles(storedToken.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // 기존 Refresh Token 무효화
        storedToken.revoke();

        // 새 토큰 생성 (tenantId, 다중 역할 포함)
        Set<String> roleNames = user.getRoles().stream()
                .map(TenantRole::name)
                .collect(Collectors.toSet());
        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                roleNames,
                user.getTenantId()
        );
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 새 Refresh Token 저장
        RefreshToken newRefreshTokenEntity = RefreshToken.create(
                newRefreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiry()
        );
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("Token refreshed: userId={}", user.getId());

        return TokenResponse.of(newAccessToken, newRefreshToken, accessTokenExpiry);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(RefreshToken::revoke);
        log.info("User logged out");
    }

    @Override
    @Transactional
    public TokenResponse switchRole(Long userId, SwitchRoleRequest request) {
        log.info("Role switch attempt: userId={}, targetRole={}", userId, request.targetRole());

        // 사용자 조회 (userRoles를 함께 로딩)
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(UserNotFoundException::new);

        // 요청한 역할을 사용자가 보유하고 있는지 확인
        if (!user.hasRole(request.targetRole())) {
            log.warn("Role switch failed: user does not have role. userId={}, targetRole={}",
                    userId, request.targetRole());
            throw new RoleNotAssignedException();
        }

        // 탈퇴/정지 사용자 체크
        if (user.getStatus() == UserStatus.WITHDRAWN || user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidCredentialsException();
        }

        // 새 토큰 생성 (currentRole을 요청한 역할로 설정)
        Set<String> roleNames = user.getRoles().stream()
                .map(TenantRole::name)
                .collect(Collectors.toSet());
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                roleNames,
                request.targetRole().name(),  // currentRole
                user.getTenantId()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 새 Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
                refreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiry()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        // 감사 로그 기록
        activityLogService.log(
                user.getId(),
                user.getName(),
                user.getEmail(),
                ActivityType.ROLE_CHANGE,
                "역할 전환: " + request.targetRole().name(),
                "USER",
                user.getId(),
                user.getName(),
                null,
                null
        );

        log.info("Role switched: userId={}, currentRole={}", userId, request.targetRole());

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpiry);
    }
}
