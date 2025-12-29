package com.mzc.lp.domain.content.repository;

import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    Optional<Content> findByIdAndTenantId(Long id, Long tenantId);

    Page<Content> findByTenantId(Long tenantId, Pageable pageable);

    Page<Content> findByTenantIdAndStatus(Long tenantId, ContentStatus status, Pageable pageable);

    Page<Content> findByTenantIdAndContentType(Long tenantId, ContentType contentType, Pageable pageable);

    Page<Content> findByTenantIdAndStatusAndContentType(Long tenantId, ContentStatus status,
                                                         ContentType contentType, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findByTenantIdAndKeyword(@Param("tenantId") Long tenantId,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.status = :status AND " +
           "(LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findByTenantIdAndStatusAndKeyword(@Param("tenantId") Long tenantId,
                                                     @Param("status") ContentStatus status,
                                                     @Param("keyword") String keyword,
                                                     Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.contentType = :contentType AND " +
           "(LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findByTenantIdAndContentTypeAndKeyword(@Param("tenantId") Long tenantId,
                                                         @Param("contentType") ContentType contentType,
                                                         @Param("keyword") String keyword,
                                                         Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.status = :status AND c.contentType = :contentType AND " +
           "(LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findByTenantIdAndStatusAndContentTypeAndKeyword(@Param("tenantId") Long tenantId,
                                                                   @Param("status") ContentStatus status,
                                                                   @Param("contentType") ContentType contentType,
                                                                   @Param("keyword") String keyword,
                                                                   Pageable pageable);

    // createdBy 기반 조회 (DESIGNER용)
    Page<Content> findByTenantIdAndCreatedBy(Long tenantId, Long createdBy, Pageable pageable);

    Page<Content> findByTenantIdAndCreatedByAndStatus(Long tenantId, Long createdBy,
                                                       ContentStatus status, Pageable pageable);

    Page<Content> findByTenantIdAndCreatedByAndContentType(Long tenantId, Long createdBy,
                                                            ContentType contentType, Pageable pageable);

    Page<Content> findByTenantIdAndCreatedByAndContentTypeAndStatus(Long tenantId, Long createdBy,
                                                                     ContentType contentType,
                                                                     ContentStatus status, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.createdBy = :createdBy " +
           "AND LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Content> findByTenantIdAndCreatedByAndKeyword(@Param("tenantId") Long tenantId,
                                                        @Param("createdBy") Long createdBy,
                                                        @Param("keyword") String keyword,
                                                        Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.createdBy = :createdBy " +
           "AND c.status = :status AND LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Content> findByTenantIdAndCreatedByAndStatusAndKeyword(@Param("tenantId") Long tenantId,
                                                                 @Param("createdBy") Long createdBy,
                                                                 @Param("status") ContentStatus status,
                                                                 @Param("keyword") String keyword,
                                                                 Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.createdBy = :createdBy " +
           "AND c.contentType = :contentType AND LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Content> findByTenantIdAndCreatedByAndContentTypeAndKeyword(@Param("tenantId") Long tenantId,
                                                                      @Param("createdBy") Long createdBy,
                                                                      @Param("contentType") ContentType contentType,
                                                                      @Param("keyword") String keyword,
                                                                      Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.createdBy = :createdBy " +
           "AND c.contentType = :contentType AND c.status = :status " +
           "AND LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Content> findByTenantIdAndCreatedByAndContentTypeAndStatusAndKeyword(@Param("tenantId") Long tenantId,
                                                                               @Param("createdBy") Long createdBy,
                                                                               @Param("contentType") ContentType contentType,
                                                                               @Param("status") ContentStatus status,
                                                                               @Param("keyword") String keyword,
                                                                               Pageable pageable);

    // folderIds 기반 조회 (LearningObject JOIN) - 하위 폴더 포함
    @Query("SELECT c FROM Content c JOIN LearningObject lo ON lo.content = c " +
           "WHERE c.tenantId = :tenantId AND c.createdBy = :createdBy " +
           "AND lo.folder.id IN :folderIds " +
           "AND (:contentType IS NULL OR c.contentType = :contentType) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(c.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findMyContentsByFolderIds(@Param("tenantId") Long tenantId,
                                             @Param("createdBy") Long createdBy,
                                             @Param("contentType") ContentType contentType,
                                             @Param("status") ContentStatus status,
                                             @Param("keyword") String keyword,
                                             @Param("folderIds") java.util.List<Long> folderIds,
                                             Pageable pageable);
}
