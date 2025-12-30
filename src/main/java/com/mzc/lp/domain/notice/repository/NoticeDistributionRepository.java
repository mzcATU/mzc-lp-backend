package com.mzc.lp.domain.notice.repository;

import com.mzc.lp.domain.notice.entity.NoticeDistribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeDistributionRepository extends JpaRepository<NoticeDistribution, Long> {

    @Query("SELECT nd FROM NoticeDistribution nd WHERE nd.notice.id = :noticeId")
    List<NoticeDistribution> findByNoticeId(@Param("noticeId") Long noticeId);

    Page<NoticeDistribution> findByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT nd FROM NoticeDistribution nd WHERE nd.notice.id = :noticeId AND nd.tenantId = :tenantId")
    Optional<NoticeDistribution> findByNoticeIdAndTenantId(@Param("noticeId") Long noticeId, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(nd) > 0 THEN true ELSE false END FROM NoticeDistribution nd WHERE nd.notice.id = :noticeId AND nd.tenantId = :tenantId")
    boolean existsByNoticeIdAndTenantId(@Param("noticeId") Long noticeId, @Param("tenantId") Long tenantId);

    @Query("SELECT nd FROM NoticeDistribution nd " +
            "JOIN nd.notice n " +
            "WHERE nd.tenantId = :tenantId AND n.status = 'PUBLISHED' " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<NoticeDistribution> findPublishedByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(nd) FROM NoticeDistribution nd WHERE nd.notice.id = :noticeId")
    long countByNoticeId(@Param("noticeId") Long noticeId);

    @Modifying
    @Query("DELETE FROM NoticeDistribution nd WHERE nd.notice.id = :noticeId")
    void deleteByNoticeId(@Param("noticeId") Long noticeId);
}
