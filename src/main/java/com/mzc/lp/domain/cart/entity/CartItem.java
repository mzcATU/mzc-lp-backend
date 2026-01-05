package com.mzc.lp.domain.cart.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_user_course_time",
                columnNames = {"tenant_id", "user_id", "course_time_id"}
        ),
        indexes = {
                @Index(name = "idx_cart_tenant_user", columnList = "tenant_id, user_id"),
                @Index(name = "idx_cart_added_at", columnList = "added_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    // ===== 정적 팩토리 메서드 =====
    public static CartItem create(Long userId, Long courseTimeId) {
        CartItem cartItem = new CartItem();
        cartItem.userId = userId;
        cartItem.courseTimeId = courseTimeId;
        cartItem.addedAt = Instant.now();
        return cartItem;
    }
}
