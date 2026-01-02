package com.mzc.lp.domain.community.repository;

import com.mzc.lp.domain.community.entity.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    Optional<CommunityCommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    @Modifying
    @Query("DELETE FROM CommunityCommentLike l WHERE l.commentId = :commentId AND l.userId = :userId")
    void deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    long countByCommentId(Long commentId);

    @Query("SELECT l.commentId FROM CommunityCommentLike l WHERE l.userId = :userId AND l.commentId IN :commentIds")
    List<Long> findLikedCommentIdsByUserIdAndCommentIdIn(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    void deleteByCommentIdIn(List<Long> commentIds);
}
