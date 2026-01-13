package com.mzc.lp.domain.assignment.repository;

import com.mzc.lp.domain.assignment.constant.AssignmentStatus;
import com.mzc.lp.domain.assignment.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // 기본 조회
    Optional<Assignment> findByIdAndTenantId(Long id, Long tenantId);

    // 차수별 과제 목록 (관리자용 - 전체 상태)
    Page<Assignment> findByCourseTimeIdAndTenantIdOrderByCreatedAtDesc(
            Long courseTimeId, Long tenantId, Pageable pageable);

    // 차수별 과제 목록 (학생용 - 발행된 것만)
    Page<Assignment> findByCourseTimeIdAndTenantIdAndStatusOrderByCreatedAtDesc(
            Long courseTimeId, Long tenantId, AssignmentStatus status, Pageable pageable);

    // 차수별 발행된 과제 목록 (학생용)
    @Query("SELECT a FROM Assignment a " +
            "WHERE a.courseTimeId = :courseTimeId " +
            "AND a.tenantId = :tenantId " +
            "AND a.status IN ('PUBLISHED', 'CLOSED') " +
            "ORDER BY a.createdAt DESC")
    Page<Assignment> findPublishedByCourseTimeId(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    // 차수별 과제 개수
    long countByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId);

    // 차수별 발행된 과제 개수
    long countByCourseTimeIdAndTenantIdAndStatus(Long courseTimeId, Long tenantId, AssignmentStatus status);

    // 차수 ID 목록으로 과제 조회 (알림용)
    @Query("SELECT a FROM Assignment a WHERE a.courseTimeId IN :courseTimeIds AND a.tenantId = :tenantId")
    List<Assignment> findByCourseTimeIdInAndTenantId(
            @Param("courseTimeIds") List<Long> courseTimeIds,
            @Param("tenantId") Long tenantId);
}
