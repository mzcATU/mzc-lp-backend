package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RefreshTokenRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.dto.response.TokenResponse;
import com.mzc.lp.domain.user.dto.response.UserResponse;

public interface AuthService {

    /**
     * 회원가입
     */
    UserResponse register(RegisterRequest request);

    /**
     * 로그인
     */
    TokenResponse login(LoginRequest request);

    /**
     * 토큰 갱신
     */
    TokenResponse refresh(RefreshTokenRequest request);

    /**
     * 로그아웃
     */
    void logout(String refreshToken);
}
