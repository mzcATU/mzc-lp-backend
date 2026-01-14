package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 사용자 ID로 모든 역할 조회
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 사용자 ID로 역할 Set 조회
     */
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    Set<TenantRole> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 특정 역할을 가진 사용자 수 조회
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role = :role AND ur.user.tenantId = :tenantId")
    long countByRoleAndTenantId(@Param("role") TenantRole role, @Param("tenantId") Long tenantId);

    /**
     * 사용자의 특정 역할 존재 여부 확인
     */
    boolean existsByUserIdAndRole(Long userId, TenantRole role);

    /**
     * 사용자의 모든 역할 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 사용자의 특정 역할 삭제
     */
    void deleteByUserIdAndRole(Long userId, TenantRole role);
}
