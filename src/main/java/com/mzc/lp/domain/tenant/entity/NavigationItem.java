package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 네비게이션 아이템 엔티티
 * 테넌트별 사이드바/네비게이션 메뉴 항목을 관리
 */
@Entity
@Table(name = "navigation_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_navigation_tenant_path", columnNames = {"tenant_id", "path"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NavigationItem extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 50)
    private String icon;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(length = 50)
    private String target; // _self, _blank

    @Column(length = 50)
    private String parentId; // 계층 구조 지원 (optional)

    // 정적 팩토리 메서드
    public static NavigationItem create(Tenant tenant, String label, String icon, String path, int displayOrder) {
        NavigationItem item = new NavigationItem();
        item.tenant = tenant;
        item.label = label;
        item.icon = icon;
        item.path = path;
        item.displayOrder = displayOrder;
        item.enabled = true;
        item.target = path.startsWith("http") ? "_blank" : "_self";
        return item;
    }

    // 기본 네비게이션 항목 생성
    public static NavigationItem createDefault(Tenant tenant, String label, String icon, String path, int order) {
        return create(tenant, label, icon, path, order);
    }

    // 업데이트 메서드
    public void update(String label, String icon, String path, Boolean enabled, Integer displayOrder, String target) {
        if (label != null) this.label = label;
        if (icon != null) this.icon = icon;
        if (path != null) {
            this.path = path;
            this.target = path.startsWith("http") ? "_blank" : "_self";
        }
        if (enabled != null) this.enabled = enabled;
        if (displayOrder != null) this.displayOrder = displayOrder;
        if (target != null) this.target = target;
    }

    // 활성/비활성 토글
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }

    // 순서 변경
    public void updateOrder(int newOrder) {
        this.displayOrder = newOrder;
    }
}
