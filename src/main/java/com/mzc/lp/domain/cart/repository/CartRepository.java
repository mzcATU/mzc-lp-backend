package com.mzc.lp.domain.cart.repository;

import com.mzc.lp.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    /**
     * 사용자의 장바구니 목록 조회 (최신순)
     */
    @Query("SELECT c FROM CartItem c WHERE c.userId = :userId ORDER BY c.addedAt DESC")
    List<CartItem> findByUserIdOrderByAddedAtDesc(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 차수 장바구니 항목 조회
     */
    Optional<CartItem> findByUserIdAndCourseTimeId(Long userId, Long courseTimeId);

    /**
     * 특정 사용자의 특정 차수 장바구니 존재 여부
     */
    boolean existsByUserIdAndCourseTimeId(Long userId, Long courseTimeId);

    /**
     * 장바구니 항목 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.courseTimeId = :courseTimeId")
    void deleteByUserIdAndCourseTimeId(@Param("userId") Long userId, @Param("courseTimeId") Long courseTimeId);

    /**
     * 여러 차수 장바구니 항목 일괄 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.courseTimeId IN :courseTimeIds")
    void deleteByUserIdAndCourseTimeIdIn(@Param("userId") Long userId, @Param("courseTimeIds") List<Long> courseTimeIds);

    /**
     * 사용자의 장바구니 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 특정 차수들의 장바구니 존재 여부 조회
     */
    @Query("SELECT c.courseTimeId FROM CartItem c WHERE c.userId = :userId AND c.courseTimeId IN :courseTimeIds")
    List<Long> findCourseTimeIdsByUserIdAndCourseTimeIdIn(@Param("userId") Long userId, @Param("courseTimeIds") List<Long> courseTimeIds);
}
