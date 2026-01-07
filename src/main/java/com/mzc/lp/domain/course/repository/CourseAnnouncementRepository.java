package com.mzc.lp.domain.course.repository;

import com.mzc.lp.domain.course.entity.CourseAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseAnnouncementRepository extends JpaRepository<CourseAnnouncement, Long> {

    /**
     * 코스 전체 공지 목록 조회 (중요 공지 우선, 최신순)
     */
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.courseId = :courseId AND a.tenantId = :tenantId " +
            "AND a.courseTimeId IS NULL " +
            "ORDER BY a.isImportant DESC, a.createdAt DESC")
    Page<CourseAnnouncement> findByCourseId(
            @Param("courseId") Long courseId,
            @Param("tenantId") Long tenantId,
            Pageable pageable
    );

    /**
     * 차수별 공지 목록 조회 (중요 공지 우선, 최신순)
     */
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.courseTimeId = :courseTimeId AND a.tenantId = :tenantId " +
            "ORDER BY a.isImportant DESC, a.createdAt DESC")
    Page<CourseAnnouncement> findByCourseTimeId(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId,
            Pageable pageable
    );

    /**
     * 코스 + 차수 공지 통합 조회 (코스 공지 + 차수 공지 모두)
     */
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.tenantId = :tenantId " +
            "AND (a.courseId = :courseId AND a.courseTimeId IS NULL) " +
            "OR (a.courseTimeId = :courseTimeId) " +
            "ORDER BY a.isImportant DESC, a.createdAt DESC")
    Page<CourseAnnouncement> findByCourseIdOrCourseTimeId(
            @Param("courseId") Long courseId,
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId,
            Pageable pageable
    );

    /**
     * 공지사항 단건 조회
     */
    Optional<CourseAnnouncement> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 코스별 공지 단건 조회
     */
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.id = :id AND a.courseId = :courseId AND a.tenantId = :tenantId")
    Optional<CourseAnnouncement> findByIdAndCourseIdAndTenantId(
            @Param("id") Long id,
            @Param("courseId") Long courseId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 차수별 공지 단건 조회
     */
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.id = :id AND a.courseTimeId = :courseTimeId AND a.tenantId = :tenantId")
    Optional<CourseAnnouncement> findByIdAndCourseTimeIdAndTenantId(
            @Param("id") Long id,
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE CourseAnnouncement a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 코스 전체 공지 개수
     */
    @Query("SELECT COUNT(a) FROM CourseAnnouncement a WHERE a.courseId = :courseId AND a.tenantId = :tenantId AND a.courseTimeId IS NULL")
    long countByCourseId(@Param("courseId") Long courseId, @Param("tenantId") Long tenantId);

    /**
     * 차수별 공지 개수
     */
    long countByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId);
}
