package com.mzc.lp.domain.snapshot.repository;

import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SnapshotRelationRepository extends JpaRepository<SnapshotRelation, Long> {

    Optional<SnapshotRelation> findByIdAndTenantId(Long id, Long tenantId);

    List<SnapshotRelation> findBySnapshotIdAndTenantId(Long snapshotId, Long tenantId);

    Optional<SnapshotRelation> findBySnapshotIdAndFromItemIsNullAndTenantId(Long snapshotId, Long tenantId);

    Optional<SnapshotRelation> findByFromItemIdAndTenantId(Long fromItemId, Long tenantId);

    Optional<SnapshotRelation> findByToItemIdAndTenantId(Long toItemId, Long tenantId);

    @Query("SELECT sr FROM SnapshotRelation sr LEFT JOIN FETCH sr.fromItem LEFT JOIN FETCH sr.toItem WHERE sr.snapshot.id = :snapshotId AND sr.tenantId = :tenantId")
    List<SnapshotRelation> findBySnapshotIdWithItems(@Param("snapshotId") Long snapshotId, @Param("tenantId") Long tenantId);

    boolean existsByToItemIdAndTenantId(Long toItemId, Long tenantId);

    boolean existsByFromItemIdAndToItemIdAndTenantId(Long fromItemId, Long toItemId, Long tenantId);

    void deleteBySnapshotIdAndTenantId(Long snapshotId, Long tenantId);
}
