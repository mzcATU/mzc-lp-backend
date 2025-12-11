package com.mzc.lp.domain.user.service;

import com.mzc.lp.common.security.JwtProvider;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RefreshTokenRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.dto.response.TokenResponse;
import com.mzc.lp.domain.user.dto.response.UserResponse;
import com.mzc.lp.domain.user.entity.RefreshToken;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.exception.DuplicateEmailException;
import com.mzc.lp.domain.user.exception.InvalidCredentialsException;
import com.mzc.lp.domain.user.exception.InvalidTokenException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiry;

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
        log.info("User registered: {}", savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 탈퇴/정지 사용자 체크
        if (user.getStatus() == UserStatus.WITHDRAWN || user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidCredentialsException();
        }

        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
                refreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiry()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in: {}", user.getEmail());

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpiry);
    }

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

        // 사용자 조회
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // 기존 Refresh Token 무효화
        storedToken.revoke();

        // 새 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 새 Refresh Token 저장
        RefreshToken newRefreshTokenEntity = RefreshToken.create(
                newRefreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiry()
        );
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("Token refreshed for user: {}", user.getEmail());

        return TokenResponse.of(newAccessToken, newRefreshToken, accessTokenExpiry);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(RefreshToken::revoke);
        log.info("User logged out");
    }
}
