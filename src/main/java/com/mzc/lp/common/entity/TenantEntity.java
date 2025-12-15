package com.mzc.lp.common.entity;

import com.mzc.lp.common.context.TenantContext;
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
            // TenantContext에서 자동 주입
            this.tenantId = TenantContext.getCurrentTenantId();
        }
    }

    /**
     * TenantId 설정 (테스트 전용)
     * 프로덕션 코드에서는 TenantContext를 통해 자동 주입됨
     */
    protected void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
