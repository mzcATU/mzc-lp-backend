package com.mzc.lp.domain.tenantnotice.repository;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.entity.TenantNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 테넌트 공지사항 리포지토리
 */
@Repository
public interface TenantNoticeRepository extends JpaRepository<TenantNotice, Long> {

    /**
     * 테넌트의 특정 공지 조회
     */
    Optional<TenantNotice> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 테넌트의 모든 공지 조회 (페이징)
     */
    Page<TenantNotice> findByTenantIdOrderByIsPinnedDescCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * 테넌트의 상태별 공지 조회
     */
    Page<TenantNotice> findByTenantIdAndStatusOrderByIsPinnedDescCreatedAtDesc(
            Long tenantId, TenantNoticeStatus status, Pageable pageable);

    /**
     * 테넌트의 대상별 공지 조회
     */
    Page<TenantNotice> findByTenantIdAndTargetAudienceOrderByIsPinnedDescCreatedAtDesc(
            Long tenantId, NoticeTargetAudience targetAudience, Pageable pageable);

    /**
     * 테넌트의 상태 + 대상별 공지 조회
     */
    Page<TenantNotice> findByTenantIdAndStatusAndTargetAudienceOrderByIsPinnedDescCreatedAtDesc(
            Long tenantId, TenantNoticeStatus status, NoticeTargetAudience targetAudience, Pageable pageable);

    /**
     * TU/CO용: 발행된 + 만료되지 않은 공지 조회
     * targetAudience가 해당 역할이거나 ALL인 공지 모두 조회
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND (n.targetAudience = :targetAudience OR n.targetAudience = 'ALL') " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now) " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<TenantNotice> findVisibleNotices(
            @Param("tenantId") Long tenantId,
            @Param("targetAudience") NoticeTargetAudience targetAudience,
            @Param("now") Instant now,
            Pageable pageable);

    /**
     * TU/CO용: 발행된 + 만료되지 않은 공지 목록 (페이징 없이)
     * targetAudience가 해당 역할이거나 ALL인 공지 모두 조회
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND (n.targetAudience = :targetAudience OR n.targetAudience = 'ALL') " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now) " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    List<TenantNotice> findVisibleNoticesList(
            @Param("tenantId") Long tenantId,
            @Param("targetAudience") NoticeTargetAudience targetAudience,
            @Param("now") Instant now);

    /**
     * 키워드 검색 (제목 또는 내용)
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.isPinned DESC, n.createdAt DESC")
    Page<TenantNotice> searchByKeyword(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 특정 사용자가 작성한 공지 조회
     */
    Page<TenantNotice> findByTenantIdAndCreatedByOrderByCreatedAtDesc(
            Long tenantId, Long createdBy, Pageable pageable);

    /**
     * 테넌트의 공지 수 카운트
     */
    long countByTenantId(Long tenantId);

    /**
     * 테넌트의 상태별 공지 수 카운트
     */
    long countByTenantIdAndStatus(Long tenantId, TenantNoticeStatus status);

    /**
     * 테넌트의 발행된 공지 수 카운트 (만료되지 않은 것)
     * targetAudience가 해당 역할이거나 ALL인 공지 모두 카운트
     */
    @Query("SELECT COUNT(n) FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND (n.targetAudience = :targetAudience OR n.targetAudience = 'ALL') " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now)")
    long countVisibleNotices(
            @Param("tenantId") Long tenantId,
            @Param("targetAudience") NoticeTargetAudience targetAudience,
            @Param("now") Instant now);

    // ============================================
    // 다중 역할 지원 쿼리 (Multiple Target Audiences)
    // ============================================

    /**
     * TU/CO용: 다중 역할에 해당하는 발행된 공지 조회 (페이징)
     * targetAudience가 사용자의 모든 역할 중 하나와 일치하는 공지 조회
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND n.targetAudience IN :targetAudiences " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now) " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<TenantNotice> findVisibleNoticesForMultipleAudiences(
            @Param("tenantId") Long tenantId,
            @Param("targetAudiences") java.util.Set<NoticeTargetAudience> targetAudiences,
            @Param("now") Instant now,
            Pageable pageable);

    /**
     * TU/CO용: 다중 역할에 해당하는 발행된 공지 수 카운트
     */
    @Query("SELECT COUNT(n) FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND n.targetAudience IN :targetAudiences " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now)")
    long countVisibleNoticesForMultipleAudiences(
            @Param("tenantId") Long tenantId,
            @Param("targetAudiences") java.util.Set<NoticeTargetAudience> targetAudiences,
            @Param("now") Instant now);

    // ============================================
    // 배포 통계용 쿼리
    // ============================================

    /**
     * 특정 상태들의 공지 조회 (발행일 기준 내림차순)
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status IN :statuses " +
            "ORDER BY n.publishedAt DESC")
    Page<TenantNotice> findByTenantIdAndStatusInOrderByPublishedAtDesc(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<TenantNoticeStatus> statuses,
            Pageable pageable);

    /**
     * 특정 상태들의 공지 수 카운트
     */
    @Query("SELECT COUNT(n) FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status IN :statuses")
    long countByTenantIdAndStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<TenantNoticeStatus> statuses);

    /**
     * 특정 상태들의 공지 viewCount 합계
     */
    @Query("SELECT SUM(n.viewCount) FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status IN :statuses")
    Long sumViewCountByTenantIdAndStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<TenantNoticeStatus> statuses);
}
