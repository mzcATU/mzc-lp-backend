package com.mzc.lp.domain.assignment.repository;

import com.mzc.lp.domain.assignment.constant.SubmissionStatus;
import com.mzc.lp.domain.assignment.entity.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // 기본 조회
    Optional<AssignmentSubmission> findByIdAndTenantId(Long id, Long tenantId);

    // 과제별 제출물 목록
    Page<AssignmentSubmission> findByAssignmentIdAndTenantIdOrderBySubmittedAtDesc(
            Long assignmentId, Long tenantId, Pageable pageable);

    // 과제별 제출물 목록 (상태 필터)
    Page<AssignmentSubmission> findByAssignmentIdAndTenantIdAndStatusOrderBySubmittedAtDesc(
            Long assignmentId, Long tenantId, SubmissionStatus status, Pageable pageable);

    // 학생별 제출물 조회 (특정 과제)
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentIdAndTenantId(
            Long assignmentId, Long studentId, Long tenantId);

    // 학생별 전체 제출물 목록
    Page<AssignmentSubmission> findByStudentIdAndTenantIdOrderBySubmittedAtDesc(
            Long studentId, Long tenantId, Pageable pageable);

    // 제출 여부 확인
    boolean existsByAssignmentIdAndStudentIdAndTenantId(Long assignmentId, Long studentId, Long tenantId);

    // 과제별 제출 카운트
    long countByAssignmentIdAndTenantId(Long assignmentId, Long tenantId);

    // 과제별 상태별 제출 카운트
    long countByAssignmentIdAndTenantIdAndStatus(Long assignmentId, Long tenantId, SubmissionStatus status);

    // 과제별 채점 완료 카운트
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s " +
            "WHERE s.assignmentId = :assignmentId " +
            "AND s.tenantId = :tenantId " +
            "AND s.status = 'GRADED'")
    long countGradedByAssignmentId(@Param("assignmentId") Long assignmentId, @Param("tenantId") Long tenantId);

    // 과제별 평균 점수
    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s " +
            "WHERE s.assignmentId = :assignmentId " +
            "AND s.tenantId = :tenantId " +
            "AND s.status = 'GRADED' " +
            "AND s.score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId, @Param("tenantId") Long tenantId);

    // 과제 ID 목록으로 제출물 조회
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignmentId IN :assignmentIds AND s.tenantId = :tenantId")
    List<AssignmentSubmission> findByAssignmentIdInAndTenantId(
            @Param("assignmentIds") List<Long> assignmentIds,
            @Param("tenantId") Long tenantId);
}
