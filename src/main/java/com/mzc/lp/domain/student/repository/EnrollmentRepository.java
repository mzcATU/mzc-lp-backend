package com.mzc.lp.domain.student.repository;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.entity.Enrollment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 기본 조회
    Optional<Enrollment> findByIdAndTenantId(Long id, Long tenantId);

    // 비관적 락 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollment e WHERE e.id = :id")
    Optional<Enrollment> findByIdWithLock(@Param("id") Long id);

    // 중복 체크
    boolean existsByUserIdAndCourseTimeIdAndTenantId(Long userId, Long courseTimeId, Long tenantId);

    // 사용자 + 차수 조회
    Optional<Enrollment> findByUserIdAndCourseTimeIdAndTenantId(Long userId, Long courseTimeId, Long tenantId);

    // 차수별 수강생 목록
    Page<Enrollment> findByCourseTimeIdAndTenantId(Long courseTimeId, Long tenantId, Pageable pageable);

    // 차수별 수강생 목록 (상태 필터)
    Page<Enrollment> findByCourseTimeIdAndStatusAndTenantId(
            Long courseTimeId, EnrollmentStatus status, Long tenantId, Pageable pageable);

    // 차수별 수강생 목록 (상태 제외 - 취소 제외 등)
    @Query("SELECT e FROM Enrollment e WHERE e.courseTimeId = :courseTimeId AND e.tenantId = :tenantId AND e.status != :excludeStatus")
    Page<Enrollment> findByCourseTimeIdAndStatusNot(
            @Param("courseTimeId") Long courseTimeId,
            @Param("tenantId") Long tenantId,
            @Param("excludeStatus") EnrollmentStatus excludeStatus,
            Pageable pageable);

    // 사용자별 수강 이력
    Page<Enrollment> findByUserIdAndTenantId(Long userId, Long tenantId, Pageable pageable);

    // 사용자별 수강 이력 (상태 필터)
    Page<Enrollment> findByUserIdAndStatusAndTenantId(
            Long userId, EnrollmentStatus status, Long tenantId, Pageable pageable);

    // 사용자별 전체 수강 목록 (정렬용)
    List<Enrollment> findByUserIdAndTenantIdOrderByEnrolledAtDesc(Long userId, Long tenantId);

    // 차수별 정원 카운트 (취소 제외)
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseTimeId = :courseTimeId AND e.tenantId = :tenantId AND e.status != 'DROPPED'")
    long countActiveByCourseTimeId(@Param("courseTimeId") Long courseTimeId, @Param("tenantId") Long tenantId);

    // 차수별 상태별 카운트
    long countByCourseTimeIdAndStatusAndTenantId(Long courseTimeId, EnrollmentStatus status, Long tenantId);

    // 사용자별 상태별 카운트
    long countByUserIdAndStatusAndTenantId(Long userId, EnrollmentStatus status, Long tenantId);

    // 사용자별 전체 카운트
    long countByUserIdAndTenantId(Long userId, Long tenantId);
}
