package com.mzc.lp.domain.user.repository;

import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    /**
     * 로그인용 이메일 조회 - 테넌트 필터 없이 전체 사용자 대상 조회
     * Native Query로 Hibernate 필터 우회
     */
    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailForLogin(@Param("email") String email);

    /**
     * 로그인용 이메일 조회 - userRoles를 함께 로딩 (다중 역할 지원)
     * JPQL LEFT JOIN FETCH로 userRoles 즉시 로딩
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userRoles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * ID로 사용자 조회 - userRoles를 함께 로딩 (토큰 갱신용)
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userRoles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    boolean existsByEmail(String email);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);

    // ===== 통계 집계 쿼리 =====

    /**
     * 테넌트별 상태별 사용자 카운트
     */
    @Query("SELECT u.status AS status, COUNT(u) AS count " +
            "FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "GROUP BY u.status")
    List<StatusCountProjection> countByTenantIdGroupByStatus(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 역할별 사용자 카운트
     */
    @Query("SELECT u.role AS type, COUNT(u) AS count " +
            "FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "GROUP BY u.role")
    List<TypeCountProjection> countByTenantIdGroupByRole(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 전체 사용자 카운트
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE tenant_id = :tenantId", nativeQuery = true)
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 신규 사용자 카운트 (특정 시점 이후 가입)
     */
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.createdAt >= :since")
    long countNewUsersSince(
            @Param("tenantId") Long tenantId,
            @Param("since") Instant since);

    /**
     * 테넌트별 활성 사용자 카운트 (ACTIVE 상태)
     */
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE'")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);

    // ===== 기간 필터 통계 쿼리 (TA 대시보드) =====

    /**
     * 테넌트별 상태별 사용자 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT u.status AS status, COUNT(u) AS count " +
            "FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.createdAt >= :startDate AND u.createdAt < :endDate " +
            "GROUP BY u.status")
    List<StatusCountProjection> countByTenantIdGroupByStatusWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 전체 사용자 카운트 (기간 필터 - createdAt 기준)
     */
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countByTenantIdWithPeriod(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 테넌트별 사용자 삭제 (테넌트 삭제 시 cascade용)
     */
    void deleteByTenantId(Long tenantId);

    /**
     * 테넌트별 활성 사용자 ID 목록 조회 (알림 발송용)
     */
    @Query("SELECT u.id FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE'")
    List<Long> findActiveUserIdsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 특정 역할의 활성 사용자 ID 목록 조회 (공지 알림 발송용)
     */
    @Query("SELECT u.id FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND u.role = :role")
    List<Long> findActiveUserIdsByTenantIdAndRole(
            @Param("tenantId") Long tenantId,
            @Param("role") com.mzc.lp.domain.user.constant.TenantRole role);

    /**
     * 테넌트별 여러 역할의 활성 사용자 ID 목록 조회 (공지 알림 발송용)
     */
    @Query("SELECT u.id FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND u.role IN :roles")
    List<Long> findActiveUserIdsByTenantIdAndRoles(
            @Param("tenantId") Long tenantId,
            @Param("roles") java.util.Collection<com.mzc.lp.domain.user.constant.TenantRole> roles);

    /**
     * 테넌트별 특정 역할을 가진 활성 사용자 ID 목록 조회 (userRoles 테이블 기준 - 다중 역할 지원)
     * 사용자의 기본 역할(u.role) 또는 추가 역할(userRoles)에 해당 역할이 있으면 포함
     */
    @Query("SELECT DISTINCT u.id FROM User u " +
            "LEFT JOIN u.userRoles ur " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND (u.role = :role OR ur.role = :role)")
    List<Long> findActiveUserIdsByTenantIdHavingRole(
            @Param("tenantId") Long tenantId,
            @Param("role") com.mzc.lp.domain.user.constant.TenantRole role);

    /**
     * 테넌트별 여러 역할 중 하나를 가진 활성 사용자 ID 목록 조회 (userRoles 테이블 기준 - 다중 역할 지원)
     */
    @Query("SELECT DISTINCT u.id FROM User u " +
            "LEFT JOIN u.userRoles ur " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND (u.role IN :roles OR ur.role IN :roles)")
    List<Long> findActiveUserIdsByTenantIdHavingAnyRole(
            @Param("tenantId") Long tenantId,
            @Param("roles") java.util.Collection<com.mzc.lp.domain.user.constant.TenantRole> roles);

    // ===== 기간 필터 통계 쿼리 (SA 대시보드) - 전체 사용자 =====

    /**
     * 기간 내 생성된 전체 사용자 조회 (테넌트 무관, createdAt 기준)
     * Native Query로 Hibernate 테넌트 필터 우회
     */
    @Query(value = "SELECT * FROM users WHERE created_at >= :startDate AND created_at < :endDate",
            nativeQuery = true)
    List<User> findAllWithPeriod(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * 전체 사용자 조회 (테넌트 무관, SA 대시보드용)
     * Native Query로 Hibernate 테넌트 필터 우회
     */
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUsers();

    // ===== 배포 통계용 쿼리 =====

    /**
     * 테넌트별 활성 사용자 수 카운트 (배포 통계용)
     */
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE'")
    long countActiveUsersByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 특정 역할의 활성 사용자 수 카운트 (배포 통계용)
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "LEFT JOIN u.userRoles ur " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND (u.role = :role OR ur.role = :role)")
    int countActiveUsersByTenantIdAndRole(
            @Param("tenantId") Long tenantId,
            @Param("role") com.mzc.lp.domain.user.constant.TenantRole role);

    /**
     * 테넌트별 활성 사용자 정보 목록 조회 (배포 상세용)
     * 반환: [userId, userName, userEmail, userRole]
     */
    @Query("SELECT u.id, u.name, u.email, CAST(u.role AS string) " +
            "FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "ORDER BY u.name")
    List<Object[]> findActiveUsersInfoByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 특정 역할의 활성 사용자 정보 목록 조회 (배포 상세용)
     * 반환: [userId, userName, userEmail, userRole]
     */
    @Query("SELECT DISTINCT u.id, u.name, u.email, CAST(u.role AS string) " +
            "FROM User u " +
            "LEFT JOIN u.userRoles ur " +
            "WHERE u.tenantId = :tenantId " +
            "AND u.status = 'ACTIVE' " +
            "AND (u.role = :role OR ur.role = :role) " +
            "ORDER BY u.name")
    List<Object[]> findActiveUsersInfoByTenantIdAndRole(
            @Param("tenantId") Long tenantId,
            @Param("role") com.mzc.lp.domain.user.constant.TenantRole role);

    // ===== 리포트 내보내기용 쿼리 =====

    /**
     * 테넌트별 사용자 조회 (기간 필터 - 리포트 내보내기용)
     */
    @Query("SELECT u FROM User u " +
            "WHERE u.tenantId = :tenantId " +
            "AND (:startDate IS NULL OR u.createdAt >= :startDate)")
    Page<User> findByTenantIdWithPeriodFilter(
            @Param("tenantId") Long tenantId,
            @Param("startDate") Instant startDate,
            Pageable pageable);
}
