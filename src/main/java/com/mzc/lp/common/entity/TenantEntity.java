package com.mzc.lp.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class TenantEntity extends BaseTimeEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @PrePersist
    protected void prePersistTenant() {
        if (this.tenantId == null) {
            this.tenantId = 1L; // 기본값: B2C 테넌트
        }
    }

    protected void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
