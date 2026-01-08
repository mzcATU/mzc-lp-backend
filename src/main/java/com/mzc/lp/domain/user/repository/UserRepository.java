package com.mzc.lp.domain.user.repository;

import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.user.entity.User;
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
    long countByTenantId(Long tenantId);

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

    // ===== 기간 필터 통계 쿼리 (SA 대시보드) - 전체 사용자 =====

    /**
     * 기간 내 생성된 전체 사용자 조회 (테넌트 무관, createdAt 기준)
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    List<User> findAllWithPeriod(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
