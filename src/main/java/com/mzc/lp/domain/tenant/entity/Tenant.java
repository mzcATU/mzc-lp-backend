package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"}),
        @UniqueConstraint(columnNames = {"subdomain"}),
        @UniqueConstraint(columnNames = {"custom_domain"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends BaseTimeEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType plan;

    @Column(nullable = false, length = 50)
    private String subdomain;

    @Column(name = "custom_domain", length = 255)
    private String customDomain;

    // 정적 팩토리 메서드
    public static Tenant create(String code, String name, TenantType type,
                                String subdomain, PlanType plan) {
        Tenant tenant = new Tenant();
        tenant.code = code;
        tenant.name = name;
        tenant.type = type;
        tenant.subdomain = subdomain;
        tenant.plan = plan != null ? plan : PlanType.FREE;
        tenant.status = TenantStatus.PENDING;
        return tenant;
    }

    public static Tenant create(String code, String name, TenantType type,
                                String subdomain, PlanType plan, String customDomain) {
        Tenant tenant = create(code, name, type, subdomain, plan);
        tenant.customDomain = customDomain;
        return tenant;
    }

    // 비즈니스 메서드
    public void update(String name, String customDomain, PlanType plan) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.customDomain = customDomain;
        if (plan != null) {
            this.plan = plan;
        }
    }

    public void changeStatus(TenantStatus status) {
        this.status = status;
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public void terminate() {
        this.status = TenantStatus.TERMINATED;
    }

    public boolean isActive() {
        return this.status == TenantStatus.ACTIVE;
    }

    public boolean isPending() {
        return this.status == TenantStatus.PENDING;
    }

    public boolean isSuspended() {
        return this.status == TenantStatus.SUSPENDED;
    }

    public boolean isTerminated() {
        return this.status == TenantStatus.TERMINATED;
    }
}
