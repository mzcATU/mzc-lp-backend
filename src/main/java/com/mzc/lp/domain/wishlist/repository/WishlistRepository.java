package com.mzc.lp.domain.wishlist.repository;

import com.mzc.lp.domain.wishlist.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * 사용자의 찜 목록 조회 (페이징)
     */
    Page<WishlistItem> findByUserId(Long userId, Pageable pageable);

    /**
     * 사용자의 찜 목록 전체 조회
     */
    List<WishlistItem> findByUserId(Long userId);

    /**
     * 특정 사용자의 특정 강의 찜 여부 확인
     */
    Optional<WishlistItem> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * 특정 사용자의 특정 강의 찜 존재 여부
     */
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * 특정 사용자의 특정 강의 찜 삭제
     */
    void deleteByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * 사용자의 찜 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 특정 강의의 찜 개수 조회
     */
    long countByCourseId(Long courseId);

    /**
     * 사용자의 여러 강의 찜 여부 일괄 확인
     */
    @Query("SELECT w.courseId FROM WishlistItem w WHERE w.userId = :userId AND w.courseId IN :courseIds")
    List<Long> findWishlistedCourseIds(@Param("userId") Long userId, @Param("courseIds") List<Long> courseIds);
}
