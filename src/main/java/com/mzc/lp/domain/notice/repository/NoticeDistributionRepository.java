package com.mzc.lp.domain.notice.repository;

import com.mzc.lp.domain.notice.entity.NoticeDistribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeDistributionRepository extends JpaRepository<NoticeDistribution, Long> {

    List<NoticeDistribution> findByNoticeId(Long noticeId);

    Page<NoticeDistribution> findByTenantId(Long tenantId, Pageable pageable);

    Optional<NoticeDistribution> findByNoticeIdAndTenantId(Long noticeId, Long tenantId);

    boolean existsByNoticeIdAndTenantId(Long noticeId, Long tenantId);

    @Query("SELECT nd FROM NoticeDistribution nd " +
            "JOIN nd.notice n " +
            "WHERE nd.tenantId = :tenantId AND n.status = 'PUBLISHED' " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<NoticeDistribution> findPublishedByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(nd) FROM NoticeDistribution nd WHERE nd.noticeId = :noticeId")
    long countByNoticeId(@Param("noticeId") Long noticeId);

    void deleteByNoticeId(Long noticeId);
}
