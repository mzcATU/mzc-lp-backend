package com.mzc.lp.domain.snapshot.repository;

import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SnapshotItemRepository extends JpaRepository<SnapshotItem, Long> {

    Optional<SnapshotItem> findByIdAndTenantId(Long id, Long tenantId);

    List<SnapshotItem> findBySnapshotIdAndTenantId(Long snapshotId, Long tenantId);

    List<SnapshotItem> findBySnapshotIdAndParentIdIsNullAndTenantId(Long snapshotId, Long tenantId);

    List<SnapshotItem> findBySnapshotIdAndParentIdAndTenantId(Long snapshotId, Long parentId, Long tenantId);

    @Query("SELECT si FROM SnapshotItem si LEFT JOIN FETCH si.snapshotLearningObject WHERE si.snapshot.id = :snapshotId AND si.tenantId = :tenantId")
    List<SnapshotItem> findBySnapshotIdWithLo(@Param("snapshotId") Long snapshotId, @Param("tenantId") Long tenantId);

    @Query("SELECT si FROM SnapshotItem si LEFT JOIN FETCH si.snapshotLearningObject WHERE si.snapshot.id = :snapshotId AND si.parent IS NULL AND si.tenantId = :tenantId")
    List<SnapshotItem> findRootItemsWithLo(@Param("snapshotId") Long snapshotId, @Param("tenantId") Long tenantId);

    @Query("SELECT si FROM SnapshotItem si WHERE si.snapshot.id = :snapshotId AND si.snapshotLearningObject IS NOT NULL AND si.tenantId = :tenantId")
    List<SnapshotItem> findItemsOnlyBySnapshotId(@Param("snapshotId") Long snapshotId, @Param("tenantId") Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    void deleteBySnapshotIdAndTenantId(Long snapshotId, Long tenantId);
}
