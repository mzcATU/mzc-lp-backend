package com.mzc.lp.domain.snapshot.repository;

import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseSnapshotRepository extends JpaRepository<CourseSnapshot, Long> {

    Optional<CourseSnapshot> findByIdAndTenantId(Long id, Long tenantId);

    Page<CourseSnapshot> findByTenantId(Long tenantId, Pageable pageable);

    Page<CourseSnapshot> findByTenantIdAndStatus(Long tenantId, SnapshotStatus status, Pageable pageable);

    Page<CourseSnapshot> findByTenantIdAndCreatedBy(Long tenantId, Long createdBy, Pageable pageable);

    Page<CourseSnapshot> findByTenantIdAndStatusAndCreatedBy(Long tenantId, SnapshotStatus status, Long createdBy, Pageable pageable);

    List<CourseSnapshot> findBySourceCourseIdAndTenantId(Long sourceCourseId, Long tenantId);

    @Query("SELECT s FROM CourseSnapshot s LEFT JOIN FETCH s.items WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<CourseSnapshot> findByIdWithItems(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT s FROM CourseSnapshot s LEFT JOIN FETCH s.relations WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<CourseSnapshot> findByIdWithRelations(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT COUNT(si) FROM SnapshotItem si WHERE si.snapshot.id = :snapshotId AND si.snapshotLearningObject IS NOT NULL")
    Long countItemsBySnapshotId(@Param("snapshotId") Long snapshotId);

    @Query("SELECT COALESCE(SUM(slo.duration), 0) FROM SnapshotItem si JOIN si.snapshotLearningObject slo WHERE si.snapshot.id = :snapshotId")
    Long sumDurationBySnapshotId(@Param("snapshotId") Long snapshotId);
}
