package com.mzc.lp.domain.community.repository;

import com.mzc.lp.domain.community.constant.PostType;
import com.mzc.lp.domain.community.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Optional<CommunityPost> findById(Long id);

    @Query("SELECT p FROM CommunityPost p WHERE " +
            "(:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:type IS NULL OR p.type = :type) " +
            "ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByFilters(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("type") PostType type,
            Pageable pageable
    );

    @Query("SELECT p FROM CommunityPost p WHERE " +
            "(:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:type IS NULL OR p.type = :type) " +
            "ORDER BY p.viewCount DESC")
    Page<CommunityPost> findByFiltersOrderByPopular(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("type") PostType type,
            Pageable pageable
    );

    @Query("SELECT p FROM CommunityPost p ORDER BY p.viewCount DESC")
    List<CommunityPost> findPopularPosts(Pageable pageable);

    @Modifying
    @Query("UPDATE CommunityPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    List<CommunityPost> findByAuthorId(Long authorId);

    @Query("SELECT p FROM CommunityPost p WHERE p.authorId = :authorId ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId, Pageable pageable);

    long countByCategory(String category);

    long countByType(PostType type);
}
