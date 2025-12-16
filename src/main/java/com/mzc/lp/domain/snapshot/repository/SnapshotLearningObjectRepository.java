package com.mzc.lp.domain.snapshot.repository;

import com.mzc.lp.domain.snapshot.entity.SnapshotLearningObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnapshotLearningObjectRepository extends JpaRepository<SnapshotLearningObject, Long> {

    Optional<SnapshotLearningObject> findByIdAndTenantId(Long id, Long tenantId);

    List<SnapshotLearningObject> findByContentIdAndTenantId(Long contentId, Long tenantId);

    List<SnapshotLearningObject> findBySourceLoIdAndTenantId(Long sourceLoId, Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    boolean existsByContentIdAndTenantId(Long contentId, Long tenantId);
}
