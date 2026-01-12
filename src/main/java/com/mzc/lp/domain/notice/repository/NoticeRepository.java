package com.mzc.lp.domain.notice.repository;

import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByStatus(NoticeStatus status, Pageable pageable);

    Page<Notice> findByType(NoticeType type, Pageable pageable);

    Page<Notice> findByStatusAndType(NoticeStatus status, NoticeType type, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.status = :status AND n.isPinned = true ORDER BY n.publishedAt DESC")
    List<Notice> findPinnedByStatus(@Param("status") NoticeStatus status);

    @Query("SELECT n FROM Notice n WHERE " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notice> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.status = :status AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notice> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                          @Param("status") NoticeStatus status,
                                          Pageable pageable);

    long countByStatus(NoticeStatus status);
}
