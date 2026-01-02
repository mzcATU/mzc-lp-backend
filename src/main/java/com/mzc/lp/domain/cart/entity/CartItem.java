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
                name = "uk_cart_user_course",
                columnNames = {"tenant_id", "user_id", "course_id"}
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

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    // ===== 정적 팩토리 메서드 =====
    public static CartItem create(Long userId, Long courseId) {
        CartItem cartItem = new CartItem();
        cartItem.userId = userId;
        cartItem.courseId = courseId;
        cartItem.addedAt = Instant.now();
        return cartItem;
    }
}
