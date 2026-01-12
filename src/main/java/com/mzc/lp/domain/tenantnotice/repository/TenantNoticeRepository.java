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
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND n.targetAudience = :targetAudience " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now) " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<TenantNotice> findVisibleNotices(
            @Param("tenantId") Long tenantId,
            @Param("targetAudience") NoticeTargetAudience targetAudience,
            @Param("now") Instant now,
            Pageable pageable);

    /**
     * TU/CO용: 발행된 + 만료되지 않은 공지 목록 (페이징 없이)
     */
    @Query("SELECT n FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND n.targetAudience = :targetAudience " +
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
     */
    @Query("SELECT COUNT(n) FROM TenantNotice n WHERE n.tenantId = :tenantId " +
            "AND n.status = 'PUBLISHED' " +
            "AND n.targetAudience = :targetAudience " +
            "AND (n.expiredAt IS NULL OR n.expiredAt > :now)")
    long countVisibleNotices(
            @Param("tenantId") Long tenantId,
            @Param("targetAudience") NoticeTargetAudience targetAudience,
            @Param("now") Instant now);
}
