package com.mzc.lp.domain.analytics.repository;

import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * 테넌트별 활동 로그 조회 (페이징)
     */
    Page<ActivityLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * 테넌트 + 활동 유형별 조회
     */
    Page<ActivityLog> findByTenantIdAndActivityTypeOrderByCreatedAtDesc(
            Long tenantId, ActivityType activityType, Pageable pageable);

    /**
     * 테넌트 + 사용자별 조회
     */
    Page<ActivityLog> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            Long tenantId, Long userId, Pageable pageable);

    /**
     * 기간별 조회
     */
    Page<ActivityLog> findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long tenantId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * 전체 시스템 로그 (SA용)
     */
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 활동 유형별 카운트 (테넌트)
     */
    @Query("SELECT a.activityType, COUNT(a) FROM ActivityLog a " +
           "WHERE a.tenantId = :tenantId AND a.createdAt >= :since " +
           "GROUP BY a.activityType")
    List<Object[]> countByActivityTypeSince(@Param("tenantId") Long tenantId, @Param("since") Instant since);

    /**
     * 일별 활동 카운트
     */
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                   "FROM activity_logs " +
                   "WHERE tenant_id = :tenantId AND created_at >= :since " +
                   "GROUP BY DATE(created_at) " +
                   "ORDER BY date DESC",
           nativeQuery = true)
    List<Object[]> countDailyActivities(@Param("tenantId") Long tenantId, @Param("since") Instant since);

    /**
     * 시간대별 활동 카운트 (오늘)
     */
    @Query(value = "SELECT HOUR(created_at) as hour, COUNT(*) as count " +
                   "FROM activity_logs " +
                   "WHERE tenant_id = :tenantId AND DATE(created_at) = CURRENT_DATE " +
                   "GROUP BY HOUR(created_at) " +
                   "ORDER BY hour",
           nativeQuery = true)
    List<Object[]> countHourlyActivitiesToday(@Param("tenantId") Long tenantId);

    /**
     * 전체 시스템 활동 유형별 카운트 (SA용)
     */
    @Query("SELECT a.activityType, COUNT(a) FROM ActivityLog a " +
           "WHERE a.createdAt >= :since " +
           "GROUP BY a.activityType")
    List<Object[]> countAllByActivityTypeSince(@Param("since") Instant since);

    /**
     * 테넌트별 활동 카운트 (SA용)
     */
    @Query("SELECT a.tenantId, COUNT(a) FROM ActivityLog a " +
           "WHERE a.createdAt >= :since " +
           "GROUP BY a.tenantId " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countByTenantSince(@Param("since") Instant since);

    /**
     * 최근 N개 로그 조회
     */
    List<ActivityLog> findTop50ByTenantIdOrderByCreatedAtDesc(Long tenantId);

    /**
     * 최근 N개 로그 조회 (SA용)
     */
    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

    /**
     * 로그인 실패 카운트 (보안 모니터링)
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a " +
           "WHERE a.userId = :userId AND a.activityType = 'LOGIN_FAILED' " +
           "AND a.createdAt >= :since")
    long countLoginFailuresSince(@Param("userId") Long userId, @Param("since") Instant since);
}
