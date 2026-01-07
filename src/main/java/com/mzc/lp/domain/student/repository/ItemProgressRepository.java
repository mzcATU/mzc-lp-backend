package com.mzc.lp.domain.student.repository;

import com.mzc.lp.domain.student.entity.ItemProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemProgressRepository extends JpaRepository<ItemProgress, Long> {

    /**
     * 수강 ID와 아이템 ID로 진도 조회
     */
    Optional<ItemProgress> findByEnrollmentIdAndItemIdAndTenantId(
            Long enrollmentId, Long itemId, Long tenantId);

    /**
     * 수강별 전체 아이템 진도 목록 조회
     */
    List<ItemProgress> findByEnrollmentIdAndTenantId(Long enrollmentId, Long tenantId);

    /**
     * 수강별 완료된 아이템 수
     */
    long countByEnrollmentIdAndCompletedAndTenantId(Long enrollmentId, Boolean completed, Long tenantId);

    /**
     * 수강별 전체 아이템 수
     */
    long countByEnrollmentIdAndTenantId(Long enrollmentId, Long tenantId);

    /**
     * 수강별 평균 진도율
     */
    @Query("SELECT AVG(ip.progressPercent) FROM ItemProgress ip " +
            "WHERE ip.enrollmentId = :enrollmentId AND ip.tenantId = :tenantId")
    Double findAverageProgressByEnrollmentId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("tenantId") Long tenantId);

    /**
     * 수강별 완료율 (완료된 아이템 / 전체 아이템 * 100)
     */
    @Query("SELECT COUNT(CASE WHEN ip.completed = true THEN 1 END) * 100.0 / NULLIF(COUNT(ip), 0) " +
            "FROM ItemProgress ip " +
            "WHERE ip.enrollmentId = :enrollmentId AND ip.tenantId = :tenantId")
    Double getCompletionRateByEnrollmentId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("tenantId") Long tenantId);

    /**
     * 특정 아이템의 완료 여부 확인
     */
    boolean existsByEnrollmentIdAndItemIdAndCompletedAndTenantId(
            Long enrollmentId, Long itemId, Boolean completed, Long tenantId);
}