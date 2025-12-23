package com.mzc.lp.domain.learning.repository;

import com.mzc.lp.domain.learning.entity.ContentFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentFolderRepository extends JpaRepository<ContentFolder, Long> {

    Optional<ContentFolder> findByIdAndTenantId(Long id, Long tenantId);

    List<ContentFolder> findByTenantIdAndParentIsNullOrderByFolderNameAsc(Long tenantId);

    List<ContentFolder> findByTenantIdAndParentIdOrderByFolderNameAsc(Long tenantId, Long parentId);

    @Query("SELECT cf FROM ContentFolder cf WHERE cf.tenantId = :tenantId ORDER BY cf.depth ASC, cf.folderName ASC")
    List<ContentFolder> findAllByTenantIdOrdered(@Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT cf FROM ContentFolder cf " +
            "LEFT JOIN FETCH cf.children " +
            "WHERE cf.tenantId = :tenantId " +
            "ORDER BY cf.depth ASC, cf.folderName ASC")
    List<ContentFolder> findAllWithChildrenByTenantId(@Param("tenantId") Long tenantId);

    boolean existsByTenantIdAndParentIdAndFolderName(Long tenantId, Long parentId, String folderName);

    boolean existsByTenantIdAndParentIsNullAndFolderName(Long tenantId, String folderName);
}
