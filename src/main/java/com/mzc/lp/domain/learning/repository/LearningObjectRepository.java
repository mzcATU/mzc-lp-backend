package com.mzc.lp.domain.learning.repository;

import com.mzc.lp.domain.learning.entity.LearningObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningObjectRepository extends JpaRepository<LearningObject, Long> {

    Optional<LearningObject> findByIdAndTenantId(Long id, Long tenantId);

    Optional<LearningObject> findByContentIdAndTenantId(Long contentId, Long tenantId);

    Page<LearningObject> findByTenantId(Long tenantId, Pageable pageable);

    Page<LearningObject> findByTenantIdAndFolderId(Long tenantId, Long folderId, Pageable pageable);

    Page<LearningObject> findByTenantIdAndFolderIsNull(Long tenantId, Pageable pageable);

    @Query("SELECT lo FROM LearningObject lo WHERE lo.tenantId = :tenantId AND " +
           "(LOWER(lo.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LearningObject> findByTenantIdAndKeyword(@Param("tenantId") Long tenantId,
                                                   @Param("keyword") String keyword,
                                                   Pageable pageable);

    @Query("SELECT lo FROM LearningObject lo WHERE lo.tenantId = :tenantId AND lo.folder.id = :folderId AND " +
           "(LOWER(lo.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LearningObject> findByTenantIdAndFolderIdAndKeyword(@Param("tenantId") Long tenantId,
                                                             @Param("folderId") Long folderId,
                                                             @Param("keyword") String keyword,
                                                             Pageable pageable);

    // 하위 폴더 포함 조회 (폴더 ID 목록으로)
    @Query("SELECT lo FROM LearningObject lo WHERE lo.tenantId = :tenantId AND lo.folder.id IN :folderIds")
    Page<LearningObject> findByTenantIdAndFolderIdIn(@Param("tenantId") Long tenantId,
                                                      @Param("folderIds") java.util.List<Long> folderIds,
                                                      Pageable pageable);

    @Query("SELECT lo FROM LearningObject lo WHERE lo.tenantId = :tenantId AND lo.folder.id IN :folderIds AND " +
           "(LOWER(lo.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LearningObject> findByTenantIdAndFolderIdInAndKeyword(@Param("tenantId") Long tenantId,
                                                               @Param("folderIds") java.util.List<Long> folderIds,
                                                               @Param("keyword") String keyword,
                                                               Pageable pageable);

    /**
     * 콘텐츠가 강의(LearningObject)에서 참조되고 있는지 확인
     */
    boolean existsByContentId(Long contentId);

    @Modifying
    @Query("DELETE FROM LearningObject lo WHERE lo.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
