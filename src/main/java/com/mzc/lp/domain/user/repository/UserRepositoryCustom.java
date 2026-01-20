package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> searchUsers(Long tenantId, String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole, Pageable pageable);

    /**
     * SYSTEM_ADMIN 역할을 가진 사용자 조회 (테넌트 필터 무시)
     */
    Page<User> searchSystemAdmins(String keyword, UserStatus status, Pageable pageable);
}
