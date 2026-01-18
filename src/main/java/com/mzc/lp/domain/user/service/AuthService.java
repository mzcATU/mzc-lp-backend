package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RefreshTokenRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.dto.request.SwitchRoleRequest;
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

    /**
     * 역할 전환
     * 사용자가 보유한 여러 역할 중 하나를 선택하여 현재 활동 역할을 변경
     * @param userId 사용자 ID
     * @param request 전환할 역할 정보
     * @return 새로운 토큰 (currentRole이 변경됨)
     */
    TokenResponse switchRole(Long userId, SwitchRoleRequest request);
}
