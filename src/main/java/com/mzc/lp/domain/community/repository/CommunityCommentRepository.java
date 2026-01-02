package com.mzc.lp.domain.community.repository;

import com.mzc.lp.domain.community.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    Optional<CommunityComment> findByIdAndPostId(Long id, Long postId);

    @Query("SELECT c FROM CommunityComment c WHERE c.postId = :postId AND c.parentId IS NULL ORDER BY c.createdAt ASC")
    Page<CommunityComment> findTopLevelCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM CommunityComment c WHERE c.postId = :postId AND c.parentId IS NULL ORDER BY c.createdAt ASC")
    List<CommunityComment> findTopLevelCommentsByPostIdList(@Param("postId") Long postId);

    List<CommunityComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    @Query("SELECT c FROM CommunityComment c WHERE c.parentId IN :parentIds ORDER BY c.createdAt ASC")
    List<CommunityComment> findByParentIdIn(@Param("parentIds") List<Long> parentIds);

    long countByPostId(Long postId);

    List<CommunityComment> findByPostId(Long postId);

    void deleteByPostId(Long postId);

    @Query("SELECT DISTINCT c.postId FROM CommunityComment c WHERE c.authorId = :authorId ORDER BY c.createdAt DESC")
    List<Long> findDistinctPostIdsByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT DISTINCT c.postId FROM CommunityComment c WHERE c.authorId = :authorId ORDER BY c.createdAt DESC")
    Page<Long> findDistinctPostIdsByAuthorIdPaged(@Param("authorId") Long authorId, Pageable pageable);

    List<CommunityComment> findByAuthorId(Long authorId);
}
