package com.mzc.lp.domain.tenant.dto.request;

import com.mzc.lp.domain.tenant.constant.PlanType;
import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(
        @Size(max = 100, message = "테넌트 이름은 100자 이하여야 합니다")
        String name,

        @Size(max = 255, message = "커스텀 도메인은 255자 이하여야 합니다")
        String customDomain,

        PlanType plan
) {
    public UpdateTenantRequest {
        if (name != null) {
            name = name.trim();
        }
        if (customDomain != null) {
            customDomain = customDomain.trim().toLowerCase();
        }
    }
}
