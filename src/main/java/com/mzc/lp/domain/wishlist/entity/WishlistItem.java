package com.mzc.lp.domain.wishlist.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 찜(위시리스트) 아이템 엔티티
 * 사용자가 관심 있는 차수(CourseTime)를 저장하는 기능
 */
@Entity
@Table(
    name = "cm_wishlist_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_wishlist_user_course_time",
            columnNames = {"tenant_id", "user_id", "course_time_id"}
        )
    },
    indexes = {
        @Index(name = "idx_wishlist_user", columnList = "tenant_id, user_id"),
        @Index(name = "idx_wishlist_course_time", columnList = "tenant_id, course_time_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistItem extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    // ===== 정적 팩토리 메서드 =====
    public static WishlistItem create(Long userId, Long courseTimeId) {
        WishlistItem item = new WishlistItem();
        item.userId = userId;
        item.courseTimeId = courseTimeId;
        return item;
    }
}
