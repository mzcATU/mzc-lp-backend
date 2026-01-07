package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테넌트별 커스텀 카테고리 엔티티
 * 각 테넌트가 자체적으로 강의 카테고리를 관리할 수 있음
 */
@Entity
@Table(name = "tenant_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "slug"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantCategory extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String icon;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean enabled = true;

    // 정적 팩토리 메서드
    public static TenantCategory create(Tenant tenant, String name, String slug,
                                        String description, String icon, Integer displayOrder) {
        TenantCategory category = new TenantCategory();
        category.tenant = tenant;
        category.name = name;
        category.slug = slug;
        category.description = description;
        category.icon = icon;
        category.displayOrder = displayOrder != null ? displayOrder : 0;
        category.enabled = true;
        return category;
    }

    // 업데이트 메서드
    public void update(String name, String slug, String description,
                       String icon, Boolean enabled) {
        if (name != null) this.name = name;
        if (slug != null) this.slug = slug;
        if (description != null) this.description = description;
        if (icon != null) this.icon = icon;
        if (enabled != null) this.enabled = enabled;
    }

    // 순서 변경 메서드
    public void updateOrder(Integer displayOrder) {
        if (displayOrder != null) this.displayOrder = displayOrder;
    }
}
