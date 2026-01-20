package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.sa.dto.request.CreateSystemAdminRequest;
import com.mzc.lp.domain.sa.dto.response.SystemAdminUserResponse;
import com.mzc.lp.domain.user.constant.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaUserService {

    /**
     * SYSTEM_ADMIN 역할을 가진 사용자 목록 조회
     */
    Page<SystemAdminUserResponse> getSystemAdmins(String keyword, UserStatus status, Pageable pageable);

    /**
     * SYSTEM_ADMIN 사용자 생성
     */
    SystemAdminUserResponse createSystemAdmin(CreateSystemAdminRequest request);
}
